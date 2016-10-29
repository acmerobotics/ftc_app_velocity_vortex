package com.acmerobotics.velocityvortex.i2c;

import android.util.Log;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;

/**
 * This class provides a public API to a <a href="https://www.sparkfun.com/products/13582">
 *     SparkFun Line Following Array</a>. The code is largely based upon
 *     <a href="https://github.com/sparkfun/SparkFun_Line_Follower_Array_Arduino_Library">
 *     this Arduino library</a>
 */
public class SX1509LineFollowingArray extends I2cDeviceSynchDevice<I2cDeviceSynch> {

	private static final String TAG = "LineFollowingArray";

	public static final I2cAddr I2CADDR_DEFAULT = I2cAddr.create7bit(0x3E);

	public Parameters parameters;

	private int lastBarRawValue;

    @SuppressWarnings("unused")
	public class Registers {
		public static final int REG_INPUT_DISABLE_B = 0x00;
		public static final int REG_INPUT_DISABLE_A = 0x01;
		public static final int REG_LONG_SLEW_B = 0x02;
		public static final int REG_LONG_SLEW_A = 0x03;
		public static final int REG_LOW_DRIVE_B = 0x04;
		public static final int REG_LOW_DRIVE_A = 0x05;
		public static final int REG_PULL_UP_B = 0x06;
		public static final int REG_PULL_UP_A = 0x07;
		public static final int REG_PULL_DOWN_B = 0x08;
		public static final int REG_PULL_DOWN_A = 0x09;
		public static final int REG_OPEN_DRAIN_B = 0x0A;
		public static final int REG_OPEN_DRAIN_A = 0x0B;
		public static final int REG_POLARITY_B = 0x0C;
		public static final int REG_POLARITY_A = 0x0D;
		public static final int REG_DIR_B = 0x0E;
		public static final int REG_DIR_A = 0x0F;
		public static final int REG_DATA_B = 0x10;
		public static final int REG_DATA_A = 0x11;
		public static final int REG_INTERRUPT_MASK_B = 0x12;
		public static final int REG_INTERRUPT_MASK_A = 0x13;
		public static final int REG_SENSE_HIGH_B = 0x14;
		public static final int REG_SENSE_LOW_B = 0x15;
		public static final int REG_SENSE_HIGH_A = 0x16;
		public static final int REG_SENSE_LOW_A = 0x17;
		public static final int REG_INTERRUPT_SOURCE_B = 0x18;
		public static final int REG_INTERRUPT_SOURCE_A = 0x19;
		public static final int REG_EVENT_STATUS_B = 0x1A;
		public static final int REG_EVENT_STATUS_A = 0x1B;
		public static final int REG_LEVEL_SHIFTER_1 = 0x1C;
		public static final int REG_LEVEL_SHIFTER_2 = 0x1D;
		public static final int REG_CLOCK = 0x1E;
		public static final int REG_MISC = 0x1F;
		public static final int REG_LED_DRIVER_ENABLE_B = 0x20;
		public static final int REG_LED_DRIVER_ENABLE_A = 0x21;
		public static final int REG_DEBOUNCE_CONFIG = 0x22;
		public static final int REG_DEBOUNCE_ENABLE_B = 0x23;
		public static final int REG_DEBOUNCE_ENABLE_A = 0x24;
		public static final int REG_KEY_CONFIG_1 = 0x25;
		public static final int REG_KEY_CONFIG_2 = 0x26;
		public static final int REG_KEY_DATA_1 = 0x27;
		public static final int REG_KEY_DATA_2 = 0x28;
		public static final int REG_T_ON_0 = 0x29;
		public static final int REG_I_ON_0 = 0x2A;
		public static final int REG_OFF_0 = 0x2B;
		public static final int REG_T_ON_1 = 0x2C;
		public static final int REG_I_ON_1 = 0x2D;
		public static final int REG_OFF_1 = 0x2E;
		public static final int REG_T_ON_2 = 0x2F;
		public static final int REG_I_ON_2 = 0x30;
		public static final int REG_OFF_2 = 0x31;
		public static final int REG_T_ON_3 = 0x32;
		public static final int REG_I_ON_3 = 0x33;
		public static final int REG_OFF_3 = 0x34;
		public static final int REG_T_ON_4 = 0x35;
		public static final int REG_I_ON_4 = 0x36;
		public static final int REG_OFF_4 = 0x37;
		public static final int REG_T_RISE_4 = 0x38;
		public static final int REG_T_FALL_4 = 0x39;
		public static final int REG_T_ON_5 = 0x3A;
		public static final int REG_I_ON_5 = 0x3B;
		public static final int REG_OFF_5 = 0x3C;
		public static final int REG_T_RISE_5 = 0x3D;
		public static final int REG_T_FALL_5 = 0x3E;
		public static final int REG_T_ON_6 = 0x3F;
		public static final int REG_I_ON_6 = 0x40;
		public static final int REG_OFF_6 = 0x41;
		public static final int REG_T_RISE_6 = 0x42;
		public static final int REG_T_FALL_6 = 0x43;
		public static final int REG_T_ON_7 = 0x44;
		public static final int REG_I_ON_7 = 0x45;
		public static final int REG_OFF_7 = 0x46;
		public static final int REG_T_RISE_7 = 0x47;
		public static final int REG_T_FALL_7 = 0x48;
		public static final int REG_T_ON_8 = 0x49;
		public static final int REG_I_ON_8 = 0x4A;
		public static final int REG_OFF_8 = 0x4B;
		public static final int REG_T_ON_9 = 0x4C;
		public static final int REG_I_ON_9 = 0x4D;
		public static final int REG_OFF_9 = 0x4E;
		public static final int REG_T_ON_10 = 0x4F;
		public static final int REG_I_ON_10 = 0x50;
		public static final int REG_OFF_10 = 0x51;
		public static final int REG_T_ON_11 = 0x52;
		public static final int REG_I_ON_11 = 0x53;
		public static final int REG_OFF_11 = 0x54;
		public static final int REG_T_ON_12 = 0x55;
		public static final int REG_I_ON_12 = 0x56;
		public static final int REG_OFF_12 = 0x57;
		public static final int REG_T_RISE_12 = 0x58;
		public static final int REG_T_FALL_12 = 0x59;
		public static final int REG_T_ON_13 = 0x5A;
		public static final int REG_I_ON_13 = 0x5B;
		public static final int REG_OFF_13 = 0x5C;
		public static final int REG_T_RISE_13 = 0x5D;
		public static final int REG_T_FALL_13 = 0x5E;
		public static final int REG_T_ON_14 = 0x5F;
		public static final int REG_I_ON_14 = 0x60;
		public static final int REG_OFF_14 = 0x61;
		public static final int REG_T_RISE_14 = 0x62;
		public static final int REG_T_FALL_14 = 0x63;
		public static final int REG_T_ON_15 = 0x64;
		public static final int REG_I_ON_15 = 0x65;
		public static final int REG_OFF_15 = 0x66;
		public static final int REG_T_RISE_15 = 0x67;
		public static final int REG_T_FALL_15 = 0x68;
		public static final int REG_HIGH_INPUT_B = 0x69;
		public static final int REG_HIGH_INPUT_A = 0x6A;
		public static final int REG_RESET = 0x7D;
		public static final int REG_TEST_1 = 0x7E;
		public static final int REG_TEST_2 = 0x7F;
	}

    /**
     * This class contains information about how the device operates
     * and is configured
     */
	public class Parameters {
        /** the I2C address of the device */
		public I2cAddr i2cAddr = I2CADDR_DEFAULT;
        /**
         * This boolean describes line illumination behavior. If true, the
         * line is illuminated only when reading the line; otherwise, the
         * line is illuminated constantly.
         */
		public boolean barStrobe = false;
        /**
         * if true, the input readings are inverted
         */
		public boolean invertBits = false;
	}

    /**
     * This constructor creates and initializes the device.
     * @param deviceClient
     */
	public SX1509LineFollowingArray(I2cDeviceSynch deviceClient) {
		super(deviceClient, true);

		this.parameters = new Parameters();

        doInitialize();
	}

    public Parameters getParameters() {
        return this.parameters;
    }

	protected void delay(int ms) {
		try {
			// delays are usually relative to preceding writes, so make sure they're all out to the controller
			this.deviceClient.waitForWriteCompletions();
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private int readWord(int ireg) {
		byte[] result = this.deviceClient.read(ireg, 2);
		int msb = ((int)result[0] & 0xFF) << 8;
		int lsb = (int)result[1] & 0xFF;
		return msb | lsb;
	}

	private int readByte(int ireg) {
		return (int)this.deviceClient.read8(ireg) & 0xFF;
	}

	@Override
	protected boolean doInitialize() {
		this.deviceClient.engage();

		this.deviceClient.setI2cAddress(parameters.i2cAddr);

		reset();

		// Communication test: read two registers with different
		// default values to verify communication
		int testRegisters = readWord(Registers.REG_INTERRUPT_MASK_A);
		Log.i(TAG, Integer.toHexString(testRegisters));
		// This should always be 0xFF00
		if (testRegisters == 0xFF00) {
			Log.i(TAG, "Connection successful");
			// Success; configure the device
			this.deviceClient.write8(Registers.REG_DIR_A, 0xFF);
			this.deviceClient.write8(Registers.REG_DIR_B, 0xFC);
			this.deviceClient.write8(Registers.REG_DATA_B, 0x01);

			this.deviceClient.waitForWriteCompletions();

			return true;
		} else {
			Log.i(TAG, "Connection unsuccessful");
			return false;
		}
	}

    /**
     * Perform a software reset of the device.
     */
	protected void reset() {
		this.deviceClient.write8(Registers.REG_RESET, 0x12);

		this.deviceClient.waitForWriteCompletions();

		this.deviceClient.write8(Registers.REG_RESET, 0x34);

		this.deviceClient.waitForWriteCompletions();
	}

    /**
     * Get the raw output of the device. This method does not query the
     * sensor directly. Call {@link #scan()} to accomplish this first.
     * @return the raw output where each bit corresponds to one of the IR sensors
     */
	public int getRaw() {
		return lastBarRawValue;
	}

    /**
     * Get the density of the device output. This method does not query
     * the sensor direcly. Call {@link #scan()} first.
     * @return the number of triggered sensors
     */
	public int getDensity() {
		int bitsCounted = 0;

		for (int i = 0; i < 8; i++) {
			if (((lastBarRawValue >> i) & 0x01) == 1) {
				bitsCounted++;
			}
		}

		return bitsCounted;
	}

    /**
     * Computes a weighted, right-left position estimate of the line. Positive
     * values correspond to sensors 0-3 and negative values correspond to sensors
     * 4-7.
     * @return the position estimate ranging from -127 to +127
     */
	public int getPosition() {
		int bitsCounted = getDensity();

		int accumulator = 0, lastBarPositionValue;

		// find the vector value of each positive bit and sum
		for (int i = 7; i > 3; i-- ) // iterate negative side bits
		{
			if ( ((lastBarRawValue >> i) & 0x01) == 1 )
			{
				accumulator += ((-32 * (i - 3)) + 1);
			}
		}
		for (int i = 0; i < 4; i++ ) // iterate positive side bits
		{
			if ( ((lastBarRawValue >> i) & 0x01) == 1 )
			{
				accumulator += ((32 * (4 - i)) - 1);
			}
		}

		if ( bitsCounted > 0 )
		{
			lastBarPositionValue = accumulator / bitsCounted;
		}
		else
		{
			lastBarPositionValue = 0;
		}

		return lastBarPositionValue;
	}

    /**
     * Read the output of the device. This method should be called
     * before attempting to access the position result with a method
     * like {@link #getRaw()}.
     */
	public void scan() {
		if (this.parameters.barStrobe) {
			this.deviceClient.write8(Registers.REG_DATA_B, 0x02); // turn on IR
			delay(2);
			this.deviceClient.write8(Registers.REG_DATA_B, 0x00); // turn on feedback
		} else {
			this.deviceClient.write8(Registers.REG_DATA_B, 0x00); // make sure both IR and indicators are on
		}

		this.deviceClient.waitForWriteCompletions();

		lastBarRawValue = readByte(Registers.REG_DATA_A); // peel the data off port A
		if (this.parameters.invertBits) {
			lastBarRawValue ^= 0xFF;
		}

		if (this.parameters.barStrobe) {
			this.deviceClient.write8(Registers.REG_DATA_B, 0x03); // turn off IR and feedback when done
			this.deviceClient.waitForWriteCompletions();
		}
	}

	@Override
	public Manufacturer getManufacturer() {
		return Manufacturer.Other;
	}

	@Override
	public String getDeviceName() {
		return "SparkFun SX1509 Line Following Array";
	}
}