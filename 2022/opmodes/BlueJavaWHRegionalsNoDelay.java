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


@Autonomous(name = "WHBlueRegionalsJavaNoDelay")
//@Disabled
public class BlueJavaWHRegionalsNoDelay extends Robot {

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

    DeadReckonPath firstPath;
    DeadReckonPath secondPath;
    DeadReckonPath carouselPath;
    DeadReckonPath initialLiftPath;
    DeadReckonPath initialPath;
    DeadReckonPath firstTierPath;
    DeadReckonPath secondTierPath;
    DeadReckonPath thirdTierPath;
    DeadReckonPath firstTierLiftPath;
    DeadReckonPath secondTierLiftPath;
    DeadReckonPath thirdTierLiftPath;
    DeadReckonPath intakePath;
    DeadReckonPath wareHousePath;

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

    public void setObjectDetection() {

        elementDetectionTask = new ObjectDetectionTask(this, "Webcam1") {
            @Override
            public void handleEvent(RobotEvent e) {
                ObjectDetectionEvent event = (ObjectDetectionEvent) e;
                objectLeft = event.objects.get(0).getLeft();
                objectMidpoint = (event.objects.get(0).getWidth()/2.0) + objectLeft;
                imageWidth = event.objects.get(0).getImageWidth();
                whereAmI.setValue("in handleEvent");
                objectConfidence = event.objects.get(0).getConfidence();
                confidenceTlm.setValue(objectConfidence);
                if (event.kind == EventKind.OBJECTS_DETECTED){
                    numObjects = event.objects.size();
                    numObjectsTlm.setValue(numObjects);
                    if (first) {
                        firstMidpoint = objectMidpoint;
                        first = false;
                        objectDetectedTlm.setValue(event.objects.get(0).getLabel());
                        randomizedMidpoint = firstMidpoint;
                    }
                    if (numObjects > 1) {
                        objectConfidence = event.objects.get(1).getConfidence();
                        confidence2Tlm.setValue(objectConfidence);
                        for (int i = 0; i < numObjects; i++) {
                            objectWidth = event.objects.get(i).getWidth();
                            objectLeft = event.objects.get(i).getLeft();
                            objectMidpoint = (objectWidth/2.0) + objectLeft;
                            if (i == 0) {
                                objectWidth1Tlm.setValue(objectWidth);
                            } else {
                                objectWidth2Tlm.setValue(objectWidth);
                            }
                            if (objectWidth < 180) {
                                //typical width seems to be 145-160 pixels but if it is greater than that it could be the background
                                if (numObjects > 1) {
                                    if (Math.abs(firstMidpoint - objectMidpoint) > 5){
                                        randomizedMidpoint = objectMidpoint;
                                        objectDetectedTlm.setValue(event.objects.get(i).getLabel());
                                    }
                                }
                                currentLocationTlm.setValue(randomizedMidpoint);
                                elementPosition = randomizedMidpoint;
                                //whereAmI.setValue("more than one object detected");
                            }
                        }
                    } else {
                        objectLeft = event.objects.get(0).getLeft();
                        objectMidpoint = (event.objects.get(0).getWidth()/2.0) + objectLeft;
                        objectDetectedTlm.setValue(event.objects.get(0).getLabel());
                        currentLocationTlm.setValue(objectMidpoint);
                        elementPosition = objectMidpoint;
                        //whereAmI.setValue("one object detected");
                    }
                }
            }
        };
        //whereAmI.setValue("setObjectDetection");
        elementDetectionTask.rateLimit(1000);
        elementDetectionTask.init(telemetry, hardwareMap);
        elementDetectionTask.setDetectionKind(ObjectDetectionTask.DetectionKind.EVERYTHING);
    }

    public void initialLift(double elementPosition)
    {
        // liftToSecondTier();
        if (elementPosition < 250) {
            liftToFirstTier();
            positionTlm.setValue("Detected in First Position");
        }else if (elementPosition < 450){
            liftToSecondTier();
            positionTlm.setValue("Detected in Second Position");
        }else if (elementPosition < 800){
            liftToThirdTier();
            positionTlm.setValue("Detected in Third Position");
        }
    }

//    public void initialJump()
//    {
//        this.addTask(new DeadReckonTask(this, initialPath, drivetrain){
//            @Override
//            public void handleEvent (RobotEvent e){
//                DeadReckonEvent path = (DeadReckonEvent) e;
//                if (path.kind == EventKind.PATH_DONE)
//                {
//                    RobotLog.i("jumped off wall");
//                    goToShippingHub(elementPosition);
//                }
//            }
//        });
//    }

    public void liftToFirstTier()
    {
        this.addTask(new DeadReckonTask(this, firstTierLiftPath, liftMotorDrivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("lifted to first tier");
                    goToFirstTier();
                }
            }
        });
    }

    public void liftToSecondTier()
    {
        this.addTask(new DeadReckonTask(this, secondTierLiftPath, liftMotorDrivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("lifted to second tier");
                    goToSecondTier();
                }
            }
        });
    }

    public void liftToThirdTier()
    {
        this.addTask(new DeadReckonTask(this, thirdTierLiftPath, liftMotorDrivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("lifted to third tier");
                    goToThirdTier();
                }
            }
        });
    }

    public void goToFirstTier()
    {
        this.addTask(new DeadReckonTask(this, firstTierPath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to shipping hub first tier");
                    depositInTier();
                }
            }
        });
    }

    public void goToSecondTier()
    {
        this.addTask(new DeadReckonTask(this, secondTierPath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to shipping hub first tier");
                    depositInTier();
                }
            }
        });
    }

    public void goToThirdTier()
    {
        this.addTask(new DeadReckonTask(this, thirdTierPath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to shipping hub first tier");
                    depositInTier();
                }
            }
        });
    }

    public void depositInTier()
    {
        this.addTask(new DeadReckonTask(this, intakePath, intakeMotorDrivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("deposited into tier");
                    parkInWarehouse();
                }
            }
        });
    }

    public void initPaths()
    {
        initialPath = new DeadReckonPath();
        initialLiftPath = new DeadReckonPath();
        firstTierPath = new DeadReckonPath();
        secondTierPath = new DeadReckonPath();
        thirdTierPath = new DeadReckonPath();
        firstTierLiftPath = new DeadReckonPath();
        secondTierLiftPath = new DeadReckonPath();
        thirdTierLiftPath = new DeadReckonPath();
        intakePath = new DeadReckonPath();
        wareHousePath = new DeadReckonPath();

        initialPath.stop();
        initialLiftPath.stop();
        firstTierPath.stop();
        secondTierPath.stop();
        thirdTierPath.stop();
        firstTierLiftPath.stop();
        secondTierLiftPath.stop();
        thirdTierLiftPath.stop();
        intakePath.stop();
        wareHousePath.stop();

        //this goes to shipping hub

        // initialPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, 0.3);

        // initialLiftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 13, -0.2);

//        shippingPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, 0.3);
//        shippingPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 16, 0.3);
//        shippingPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10.3, 0.3);

        firstTierLiftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -0.7);// distance 5
        secondTierLiftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 24, -0.7);
        thirdTierLiftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 41, -0.7);

        //firstTierPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, 0.5);
        firstTierPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 16, 0.5);
        firstTierPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 0.5);

        //secondTierPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, 0.5);
        secondTierPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 16, 0.5);
        secondTierPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, 0.5);

        //thirdTierPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,3, 0.5);
        thirdTierPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 16, 0.5);
        thirdTierPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, 0.5);

        intakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -1);

        wareHousePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 16, -0.5);
        wareHousePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 34, -0.5);


    }

    public void parkInWarehouse()
    {
        this.addTask(new DeadReckonTask(this, wareHousePath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("parked in warehouse");
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

        objectImageInfo = new ObjectImageInfo();
        objectImageInfo.displayTelemetry(this.telemetry);

        objectDetectedTlm = telemetry.addData("Object detected","unknown");
        currentLocationTlm = telemetry.addData("Current location",600); //Go to top tier
        imageTlm = telemetry.addData("Image Width",-1);

        positionTlm = telemetry.addData("Position:","unknown");

        whereAmI = telemetry.addData("location in code", "init");

        confidenceTlm = telemetry.addData("confidence", "none");
        confidence2Tlm = telemetry.addData("confidence2", "none");

        numObjectsTlm = telemetry.addData("numObjectsDetected", "none");

        objectWidth1Tlm = telemetry.addData("object1Width", "none");
        objectWidth2Tlm = telemetry.addData("object2Width", "none");

        initPaths();

        setObjectDetection();
        addTask(elementDetectionTask);
    }

    @Override
    public void start()
    {
        initialLift(elementPosition);

    }
}