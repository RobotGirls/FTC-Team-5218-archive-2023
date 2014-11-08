#pragma config(Hubs,  S1, HTMotor,  HTMotor,  HTMotor,  HTServo)
#pragma config(Hubs,  S4, HTMotor,  none,     none,     none)
#pragma config(Sensor, S2,     HTMC,           sensorI2CCustom)
#pragma config(Sensor, S3,     HTPB,           sensorI2CCustom9V)
#pragma config(Motor,  motorA,          leftspod,      tmotorNXT, PIDControl, reversed, encoder)
#pragma config(Motor,  motorB,          rightspod,     tmotorNXT, PIDControl)
#pragma config(Motor,  motorC,           ,             tmotorNXT, openLoop)
#pragma config(Motor,  mtr_S1_C1_1,     conveyor,      tmotorTetrix, openLoop)
#pragma config(Motor,  mtr_S1_C1_2,     leftElevator,  tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_1,     driveRearRight, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Motor,  mtr_S1_C2_2,     rightElevator, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_1,     driveRearLeft, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_2,     flag,          tmotorTetrix, openLoop)
#pragma config(Motor,  mtr_S4_C1_1,     driveFrontLeft, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Motor,  mtr_S4_C1_2,     driveFrontRight, tmotorTetrix, PIDControl, encoder)
#pragma config(Servo,  srvo_S1_C4_1,    blockDump,            tServoStandard)
#pragma config(Servo,  srvo_S1_C4_2,    servo2,               tServoNone)
#pragma config(Servo,  srvo_S1_C4_3,    rightHopper,          tServoStandard)
#pragma config(Servo,  srvo_S1_C4_4,    leftHopper,           tServoStandard)
#pragma config(Servo,  srvo_S1_C4_5,    middleElev,           tServoContinuousRotation)
#pragma config(Servo,  srvo_S1_C4_6,    right,                tServoContinuousRotation)
//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

#include "..\library\sensors\drivers\hitechnic-compass.h"

bool done;

task rotate()
{
	motor[driveFrontRight] = 10;
	motor[driveFrontLeft] = -10;
	motor[driveRearRight] = 10;
	motor[driveRearLeft] = -10;
	wait1Msec(40000);
	motor[driveFrontRight] = 0;
	motor[driveFrontLeft] = 0;
	motor[driveRearRight] = 0;
	motor[driveRearLeft] = 0;
    done = true;
}

task main()
{
    done = false;

	eraseDisplay();

    nMotorEncoder[driveFrontRight] = 0;
    nMotorEncoder[driveFrontLeft] = 0;

	HTMCstartCal(HTMC);
	StartTask(rotate);

    while (!done) {
        nxtDisplayTextLine(2, "Right: %d", nMotorEncoder[driveFrontRight]);
        nxtDisplayTextLine(3, "Left:  %d", nMotorEncoder[driveFrontLeft]);
    }
	HTMCstopCal(HTMC);

    while (true) {}
}