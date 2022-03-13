/*
Copyright (c) September 2017 FTC Teams 25/5218

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of FTC Teams 25/5218 nor the names of their contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.ObjectDetectionTask;
import team25core.ObjectImageInfo;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.TouchSensorCriteria;


@Autonomous(name = "SURedRegionalsJavaNoDetection")
//@Disabled
public class RedJavaRegionalsNoDetection extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private DcMotor carouselMech;
    private OneWheelDirectDrivetrain singleMotorDrivetrain;
    private OneWheelDirectDrivetrain liftMotorDrivetrain;
    private OneWheelDirectDrivetrain intakeMotorDrivetrain;
    private DcMotor liftMotor;
    private DcMotor intakeMotor;

    private FourWheelDirectDrivetrain drivetrain;

    private Telemetry.Item currentLocationTlm;
    private Telemetry.Item positionTlm;
    private Telemetry.Item objectDetectedTlm;
    private Telemetry.Item imageTlm;
    private Telemetry.Item whereAmI;

    private Telemetry.Item objectWidth1Tlm;
    private Telemetry.Item objectWidth2Tlm;

    private Telemetry.Item confidenceTlm;
    private double objectConfidence;
    private Telemetry.Item confidence2Tlm;
    private Telemetry.Item numObjectsTlm;

    private double objectLeft;
    private double objectMidpoint;
    private double firstMidpoint;
    private double randomizedMidpoint;
    private boolean first = true;
    private double objectWidth;
    private double imageWidth;

    private double elementPosition;

    private int numObjects;

    ObjectDetectionTask elementDetectionTask;
    ObjectImageInfo objectImageInfo;

    private TouchSensor touchCarousel;
    private TouchSensorCriteria touchCarouselCriteria;

    private Telemetry.Item touchSensorTlm;

    DeadReckonPath firstPath;
    DeadReckonPath secondPath;
    DeadReckonPath carouselPath;
//    DeadReckonPath initialLiftPath;
//    DeadReckonPath initialPath;
//    DeadReckonPath firstTierPath;
//    DeadReckonPath secondTierPath;
//    DeadReckonPath thirdTierPath;
//    DeadReckonPath firstTierLiftPath;
//    DeadReckonPath secondTierLiftPath;
//    DeadReckonPath thirdTierLiftPath;
//    DeadReckonPath intakePath;

    /*
     * The default event handler for the robot.
     */
    @Override
    public void handleEvent(RobotEvent e)
    {
        /*
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }

    public void initPaths()
    {
        firstPath = new DeadReckonPath();
        secondPath = new DeadReckonPath();
        carouselPath = new DeadReckonPath();

        firstPath.stop();
        secondPath.stop();
        carouselPath.stop();


        //this goes to shipping hub

        // initialPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, 0.3);

        // initialLiftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 13, -0.2);

//        shippingPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, 0.3);
//        shippingPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 16, 0.3);
//        shippingPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10.3, 0.3);


        //this path goes to the carousel
        firstPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 32, -0.4);
//        firstPath.addSegment(DeadReckonPath.SegmentType.TURN, 15, -0.6);

        carouselPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 80, 0.8);

//        secondPath.addSegment(DeadReckonPath.SegmentType.TURN, 7, 0.6);
        secondPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 0.7);
        secondPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, -0.6);

    }

    public void goToCarousel(DeadReckonPath firstPath)
    {
        this.addTask(new DeadReckonTask(this, firstPath, drivetrain, touchCarouselCriteria){
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                switch (path.kind )
                {
                    case PATH_DONE:
                        RobotLog.i("went forward to carousel");
                        spinCarousel();
                        break;
                    case SENSOR_SATISFIED:
                        RobotLog.i("went forward to carousel");
                        spinCarousel();
                        touchSensorTlm.setValue("pressed");
                        this.disableSensors();
                        break;
                }
            }
        });
    }

    public void spinCarousel()
    {
        this.addTask(new DeadReckonTask(this, carouselPath, singleMotorDrivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("spun carousel");
                    parkInStorageUnit();
                }
            }
        });
    }

    public void parkInStorageUnit()
    {
        this.addTask(new DeadReckonTask(this, secondPath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("parked in storage unit");
                }
            }
        });
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        carouselMech = hardwareMap.get(DcMotor.class, "carouselMech");
        liftMotor = hardwareMap.get(DcMotor.class,"liftMotor");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");

        liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        carouselMech.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        singleMotorDrivetrain = new OneWheelDirectDrivetrain(carouselMech);
        singleMotorDrivetrain.resetEncoders();
        singleMotorDrivetrain.encodersOn();

        liftMotorDrivetrain = new OneWheelDirectDrivetrain(liftMotor);
        liftMotorDrivetrain.resetEncoders();
        liftMotorDrivetrain.encodersOn();

        intakeMotorDrivetrain = new OneWheelDirectDrivetrain(intakeMotor);
        intakeMotorDrivetrain.resetEncoders();
        intakeMotorDrivetrain.encodersOn();

        //sensors
        touchCarousel = hardwareMap.get(TouchSensor.class, "touchCarousel");
        touchCarouselCriteria = new TouchSensorCriteria(touchCarousel);

        initPaths();

    }

    @Override
    public void start()
    {
       goToCarousel(firstPath);
        //initialLift(elementPosition);

    }
}