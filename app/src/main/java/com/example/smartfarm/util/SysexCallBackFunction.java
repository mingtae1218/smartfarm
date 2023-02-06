package com.example.smartfarm.util;

public interface SysexCallBackFunction {
	void call(byte command, byte argc, byte[] argv);
}
