/*
 * Copyright (c) September 2017 FTC Teams 25/5218
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted (subject to the limitations in the disclaimer below) provided that
 *  the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list
 *  of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *  list of conditions and the following disclaimer in the documentation and/or
 *  other materials provided with the distribution.
 *
 *  Neither the name of FTC Teams 25/5218 nor the names of their contributors may be used to
 *  endorse or promote products derived from this software without specific prior
 *  written permission.
 *
 *  NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 *  LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 *  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.StandardFourMotorRobot;
import team25core.TankMechanumControlScheme;
import team25core.TankMechanumControlSchemeReverse;
import team25core.TeleopDriveTask;

@TeleOp(name = "JavaTeleop")
//@Disabled
public class JavaTeleop extends StandardFourMotorRobot {

    private Telemetry.Item buttonTlm;

    private TeleopDriveTask drivetask;
    private DcMotor carouselMech;
    private DcMotor liftMotor;
    private DcMotor intakeMotor;

    private DeadmanMotorTask liftMotorUpTask;
    private DeadmanMotorTask liftMotorDownTask;

    private DeadmanMotorTask intakeTask;
    private DeadmanMotorTask outtakeTask;

    private MechanumGearedDrivetrain drivetrain;

    private static final int TICKS_PER_INCH = 79;

    @Override
    public void handleEvent(RobotEvent e) {
    }

    @Override
    public void init() {

        super.init();

        //mechanisms
        carouselMech = hardwareMap.get(DcMotor.class, "carouselMech");
        liftMotor = hardwareMap.get(DcMotor.class,"liftMotor");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");

        // using encoders to record ticks

        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //telemetry
        buttonTlm = telemetry.addData("buttonState", "unknown");

        TankMechanumControlSchemeReverse scheme = new TankMechanumControlSchemeReverse(gamepad1);
        drivetrain = new MechanumGearedDrivetrain(motorMap);
        drivetrain.setCanonicalMotorDirection();
        // Note we are swapping the rights and lefts in the arguments below
        // since the gamesticks were switched for some reason and we need to do
        // more investigation
        drivetask = new TeleopDriveTask(this, scheme, backLeft, backRight, frontLeft, frontRight);

        liftMotorUpTask = new DeadmanMotorTask(this, liftMotor,  -0.5, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.LEFT_STICK_UP);
        liftMotorDownTask    = new DeadmanMotorTask(this, liftMotor, 0.5, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.LEFT_STICK_DOWN);
        intakeTask = new DeadmanMotorTask(this, intakeMotor,  -0.5, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_STICK_UP);
        outtakeTask    = new DeadmanMotorTask(this, intakeMotor, 0.5, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_STICK_DOWN);
    }

    @Override
    public void start() {

        //Gamepad 1
        this.addTask(drivetask);

        //Gamepad 2
        this.addTask(liftMotorUpTask);
        this.addTask(liftMotorDownTask);
        this.addTask(intakeTask);
        this.addTask(outtakeTask);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            //@Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;

                switch (gamepadEvent.kind) {
                    case BUTTON_X_DOWN:
                        //enable carouselMech
                        carouselMech.setDirection(DcMotorSimple.Direction.FORWARD);
                        carouselMech.setPower(0.2);
                        break;
                    case BUTTON_X_UP:
                    case BUTTON_Y_UP:
                        //disable carouselMech
                        carouselMech.setPower(0);
                        break;
                    case BUTTON_Y_DOWN:
                        //enable carouselMech
                        carouselMech.setDirection(DcMotorSimple.Direction.REVERSE);
                        carouselMech.setPower(0.2);
                        break;
                    default:
                        buttonTlm.setValue("Not Moving");
                        break;
                    }
            }
        });
    }
}

