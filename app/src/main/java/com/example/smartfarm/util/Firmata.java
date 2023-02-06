package com.example.smartfarm.util;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;

public class Firmata {
	//////////////////////////////////////////////////////////////////////////////
	// command|pin LSB MSB (3BYTE)                             attach    send
	public static final int ANALOG_MESSAGE =          0xE0; //   O        O
	public static final int DIGITAL_MESSAGE =         0x90; //   O        O
	// command|pin first   (2BYTE)
	public static final int REPORT_ANALOG =           0xC0; //   X        O
	public static final int REPORT_DIGITAL =          0xD0; //   X        O   
	// command pin value   (3BYTE)
	public static final int SET_PIN_MODE =            0xF4; //   X        O
	public static final int SET_DIGITAL_PIN_VALUE =   0xF5; //   X        O
	// command             (1BYTE)
	public static final int REPORT_VERSION =          0xF9; //   O        O
	public static final int SYSTEM_RESET =            0xFF; //   X        O
	// F0 subcommand payload F7  (가변길이)
	public static final int START_SYSEX =             0xF0; //   O        O
	public static final int END_SYSEX =               0xF7;
	// subcommand
	public static final int STRING_DATA =             0x71; //   O        O
	public static final int STEP_MOTOR_DATA =         0x11; //   X        O
	//////////////////////////////////////////////////////////////////////////////
	// SET_PIN_MODE
	public static final int INPUT		 = 0;
	public static final int OUTPUT 		 = 1;
	public static final int INPUT_PULLUP = 2;
	// REPORT_ANALOG REPORT_DIGITAL
	public static final int DISABLE  = 0;
	public static final int ENABLE   = 1;
	// DIGITAL_MESSAGE SET_DIGITAL_PIN_VALUE
	public static final int LOW   = 0;
	public static final int HIGH  = 1;
	//////////////////////////////////////////////////////////////////////////////
	// attach
	CallBackFunction 		currentAnalogCallback;
	CallBackFunction 		currentDigialCallback;
	CallBackFunctionVersion currentVersionCallback;
	SysexCallBackFunction	currentSysexCallback;
	StringCallBackFunction	currentStringCallback;
	//////////////////////////////////////////////////////////////////////////////

	Bluetooth com;
	
//	LinkedList<Byte> buffer = new LinkedList<>();
	ArrayList<Byte> buffer = new ArrayList<>();
	
	public Firmata(Bluetooth com) {
		this.com = com;
	}
	
	/**
	 * public void attach(CallBackFunctionVersion versionCallback)
	 */
	public void attach(CallBackFunctionVersion versionCallback) {
		this.currentVersionCallback = versionCallback;
	}
	/**
	 * public void attach(int command, CallBackFunction callBack)
	 */
	public void attach(int command, CallBackFunction callBack) {
		switch (command) {
		case ANALOG_MESSAGE:
			this.currentAnalogCallback = callBack;
			break;
		case DIGITAL_MESSAGE:
			this.currentDigialCallback = callBack;
			break;
			
		default:
			break;
		}
	}
	/**
	 * public void attach(int command, SysexCallBackFunction sysexCallback)
	 */
	public void attach(int command, SysexCallBackFunction sysexCallback) {
		switch (command) {
		case START_SYSEX:
			this.currentSysexCallback = sysexCallback;
			break;

		default:
			break;
		}
	}
	/**
	 * public void attach(int command, StringCallBackFunction stringCallback)
	 */
	public void attach(int command, StringCallBackFunction stringCallback) {
		switch (command) {
		case STRING_DATA:
			this.currentStringCallback = stringCallback;
			break;

		default:
			break;
		}
	}
	/**
	 * public void sendAnalog(int pin, int value)
	 */
	public void sendAnalog(int pin, int value) {
		com.send(encode(ANALOG_MESSAGE, pin, value));
	}
	/**
	 * public void sendDigital(int pin, int value)
	 */
	public void sendDigital(int pin, int value) {
		com.send(encode(DIGITAL_MESSAGE, pin, value));
	}
	/**
	 * public void sendReportAnalog(int pin, int tf)
	 */
	public void sendReportAnalog(int pin, int tf) {
		byte[] packet = new byte[2];
		packet[0] = (byte)(REPORT_ANALOG|(0x0F&pin));
		packet[1] = (byte)tf;
		com.send(packet);
	}
	/**
	 * public void sendReportDigital(int pin, int tf)
	 */
	public void sendReportDigital(int pin, int tf) {
		byte[] packet = new byte[2];
		packet[0] = (byte)(REPORT_DIGITAL|(0x0F&pin));
		packet[1] = (byte)tf;
		com.send(packet);
	}
	/**
	 * public void sendSetPinMode(int pin, int mode)
	 */
	public void sendSetPinMode(int pin, int mode) {
		byte[] packet = new byte[3];
		packet[0] = (byte)(SET_PIN_MODE);
		packet[1] = (byte)pin;
		packet[2] = (byte)mode;
		com.send(packet);
	}
	/**
	 * public void sendSetDigitalPinValue(int pin, int value)
	 */
	public void sendSetDigitalPinValue(int pin, int value) {
		byte[] packet = new byte[3];
		packet[0] = (byte)(SET_DIGITAL_PIN_VALUE);
		packet[1] = (byte)pin;
		packet[2] = (byte)value;
		com.send(packet);
	}
	
	/**
	 * public void sendRequestVersion()
	 */
	public void sendRequestVersion() {
		com.send(new byte[] {(byte)REPORT_VERSION});
	}
	/**
	 * public void sendSystemReset()
	 */
	public void sendSystemReset() {
		com.send(new byte[] {(byte)SYSTEM_RESET});
	}
	/**
	 * public void sendSysex(byte command, byte argc, byte[] argv)
	 */
	public void sendSysex(byte command, byte argc, byte[] argv) {
		byte[] packet = new byte[1+1+argc*2+1];
		packet[0] = (byte)START_SYSEX;
		packet[1] = command;
		/*
		 * payload encode
		 */
		for (int i=0; i<argc; i++) {
			packet[2+2*i] = (byte)(argv[i] & 0b0111_1111);
			packet[2+2*i+1] = (byte)((argv[i] >> 7) & 0b0000_0001);
		}
		
		packet[packet.length-1] = (byte)END_SYSEX;

		com.send(packet);
	}
	/**
	 * public void sendString(String str)
	 */
	public void sendString(String str) {
		byte argc = (byte)str.length();
		byte[] argv= str.getBytes();
		sendSysex((byte)STRING_DATA, argc, argv);
	}
	/*
	 * public QByteArray encode(int command, int pin, int value)
	 * 
	 * ANANLOG_MESSAGE
	 * DIGITAL_MESSAGE
	 * 
	 * int -> byte[2]
	 */
	public byte[] encode(int command, int pin, int value) {
		byte[] packet = new byte[3];
		packet[0] = ((byte)(command | (0x0F&pin)));
		packet[1] = ((byte)(0b0111_1111 &  value));
		packet[2] = ((byte)(0b0111_1111 & (value >> 7)));
		return packet;
	}
	/**
	 * public int decode(byte byte1, byte byte2)
	 * 
	 * byte[2] -> int
	 */
	public int decode(byte byte1, byte byte2) {
		return (byte2 << 7) | byte1;
	}

	/**
	 * public void processInput(QByteArray data)
	 */
	public void processInput(byte[] data) {
		for (int i=0; i<data.length; i++)
			buffer.add(data[i]);
		
		printLog("전");
		
		if (buffer.size() < 3)
			return;
		
		while (buffer.get(0) >= 0) {
			buffer.remove(0);
		}
		
		while (buffer.size()>=3) {
			byte command = buffer.get(0);
			
			if (command == (byte)REPORT_VERSION) {
				byte major = buffer.get(1);
				byte minor = buffer.get(2);
				
				System.out.printf("Firmata V%d.%d\n", major, minor);
				
				buffer.subList(0, 3).clear();
				
				if (currentVersionCallback!=null) {
					currentVersionCallback.call(major, minor);
				}
				
			} else if ((command&0xF0)==ANALOG_MESSAGE) {
				byte pin = (byte)(command&0x0F);
				byte byte1 = buffer.get(1);
				byte byte2 = buffer.get(2);
				
				int value = decode(byte1, byte2);
				
				System.out.printf("(%2d, %2X, %2X, %4d)", pin, byte1, byte2, value);

				buffer.subList(0, 3).clear();
				
				if (currentAnalogCallback!=null)
					currentAnalogCallback.call(pin, value);
			} else if ((command&0xF0)==DIGITAL_MESSAGE) {
				byte pin = (byte)(command&0x0F);
				byte byte1 = buffer.get(1);
				byte byte2 = buffer.get(2);
				
				int value = decode(byte1, byte2);
				
				System.out.printf("(%2d, %2X, %2X, %4d)", pin, byte1, byte2, value);
				
				buffer.subList(0, 3).clear();
				
				if (currentDigialCallback!=null)
					currentDigialCallback.call(pin, value);
			} else if (command == (byte)START_SYSEX) {
				if (!processSysexCommand())
					break;
			} else {
				buffer.remove(0);
			}
		}
		
		System.out.println();
		printLog("후");
	}
	
	boolean processSysexCommand() {
		int endindex = buffer.indexOf((byte)END_SYSEX);
		if (endindex < 0)
			return false;
		
		// 찾았다. END_SYSEX(0xF7)
		List<Byte> packet = buffer.subList(0, endindex+1);
		
		byte subCommand = packet.get(1);
		
		byte[] payload = new byte[packet.size()-1-1-1];
		for (int i=0; i<payload.length; i++) {
			payload[i] = packet.get(i+2);
		}

		packet.clear();
		
		if (subCommand == STRING_DATA) {
			if (currentStringCallback != null) {
				byte[] decodeByte = new byte[payload.length/2];
				
				for (int i=0; i<decodeByte.length; i++) 
					decodeByte[i] = (byte)(payload[i*2] | (payload[i*2+1] << 7));
				
				String str = new String(decodeByte);
				currentStringCallback.call(str);
			}
			
		} else if (currentSysexCallback != null) {
			currentSysexCallback.call(subCommand, (byte)payload.length, payload);
		}
		
//		System.out.print("===[");
//		for (int i=0; i<packet.size(); i++)
//			System.out.printf("%02X ", packet.at(i));
//		System.out.println("]===");
			
		return true;	
	}
	
	void printLog(String prefix) {
		System.out.print(prefix + "[");
		for (var d : buffer)
			System.out.printf("%02X ", d);
		System.out.println("]");
	}
	/**
	 * public static int map(int x, int in_min, int in_max, int out_min, int out_max)
	 */
	public static int map(int x, int in_min, int in_max, int out_min, int out_max) {
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

}
