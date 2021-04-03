package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.RobotEvent;
import team25core.Robot;
import team25core.SingleShotTimerTask;

@Autonomous(name = "JavaBotsScrimmage3", group = "Team 5218")
//@Disabled
public class JavaAutonomous extends Robot {
//The name is JavaBotsScrimmage3 for autonomous opmodes on the phone

    private MechanumGearedDrivetrain drivetrain1;
    private Telemetry.Item loggingTlm;
    private DeadReckonPath launchLinePath;
    private final double STRAIGHT_SPEED = 0.5;
    /*private DeadReckonPath powerShotPath1;
    private DeadReckonPath powerShotPath2;
    private DeadReckonPath powerShotPath3;*/

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private DcMotor launchMechRight;
    //private DcMotor launchMechLeft;
    private Servo dispenserMech;
    private boolean dispenserMechIsOpen = false;
    private final double OPEN_SERVO = (float) 256.0/256.0;
    private final double CLOSE_SERVO = (float) 128.0/256.0;
    private int launchCounter = 0;

    private final static int POWERSHOT_TIMER = 1500;
    private final static int DEADRECKON_DELAY = 2500;

    DeadReckonPath path = new DeadReckonPath();

    // declaring gamepad variables
    //variables declarations have lowercase then uppercase
    private GamepadTask gamepad;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent) e).segment_num);
        }
    }

    public void shootPowerShot()
    {
        SingleShotTimerTask timerTask = new SingleShotTimerTask(this, POWERSHOT_TIMER){
            @Override
            public void handleEvent(RobotEvent e){
                SingleShotTimerTask.SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED) {
                    launchMechRight.setPower(0);
                    //launchMechLeft.setPower(0);
                    closeRingDispenser();
                }
            }
        };
        this.addTask(timerTask);
        //might need to reorder or rearrange actions in this method based on testing
        RobotLog.i("hits the power shot");
        openRingDispenser();
        launchMechRight.setPower(1.0);
        //launchMechLeft.setPower(-1.0);
    }

    public void openRingDispenser()
    {
        dispenserMech.setPosition(OPEN_SERVO);
        dispenserMechIsOpen = true;
    }

    public void closeRingDispenser()
    {
        dispenserMech.setPosition(CLOSE_SERVO);
        dispenserMechIsOpen = false;
    }

    public void parkOnLaunchLine()
    {
        RobotLog.i("drives straight onto the launch line");

        this.addTask(new DeadReckonTask(this, launchLinePath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("finished parking");
                }
            }
        });
    }

    /*public void goToPowerShotLocation1()
    {
        RobotLog.i("drives to PowerShot Location");

        this.addTask(new DeadReckonTask(this, powerShotPath1, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("reached PowerShot Location");
                    shootPowerShot();

                     Put in delay.

                    this.robot.addTask(new SingleShotTimerTask(this.robot, DEADRECKON_DELAY) {
                        @Override
                        public void handleEvent(RobotEvent e) {
                            goToPowerShotLocation2();
                        }
                    });
                }
            }
        });
    }*/

    /*public void goToPowerShotLocation2()
    {
        this.addTask(new DeadReckonTask(this, powerShotPath2, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("reached PowerShot Location");
                    shootPowerShot();

                     Put in delay.

                    this.robot.addTask(new SingleShotTimerTask(this.robot, DEADRECKON_DELAY) {
                        @Override
                        public void handleEvent(RobotEvent e) {
                            goToPowerShotLocation3();
                        }
                    });
                }
            }
        });
    }*/

    /*public void goToPowerShotLocation3()
    {
        this.addTask(new DeadReckonTask(this, powerShotPath3, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("reached PowerShot Location");
                    shootPowerShot();

                     Put in delay.

                    this.robot.addTask(new SingleShotTimerTask(this.robot, DEADRECKON_DELAY) {
                        @Override
                        public void handleEvent(RobotEvent e) {
                            parkOnLaunchLine();
                        }
                    });
                }
            }
        });
    }*/

    public void loop()
    {
        super.loop();
    }


    public void initPath()
    {
        launchLinePath = new DeadReckonPath();
        launchLinePath.stop();
        launchLinePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 70, -STRAIGHT_SPEED);
       /*powerShotPath1 = new DeadReckonPath();
        powerShotPath1.stop();
        powerShotPath1.addSegment(DeadReckonPath.SegmentType.TURN, 15, -STRAIGHT_SPEED);
        powerShotPath1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 100, -STRAIGHT_SPEED);
        powerShotPath2 = new DeadReckonPath();
        powerShotPath2.stop();
        powerShotPath2.addSegment(DeadReckonPath.SegmentType.TURN, 15, -STRAIGHT_SPEED);
        powerShotPath2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 100, -STRAIGHT_SPEED);
        powerShotPath3 = new DeadReckonPath();
        powerShotPath3.stop();
        powerShotPath3.addSegment(DeadReckonPath.SegmentType.TURN, 15, -STRAIGHT_SPEED);
        powerShotPath3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 100, -STRAIGHT_SPEED);*/
    }


    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class,"frontLeft");
        frontRight = hardwareMap.get(DcMotor.class,"frontRight");
        backLeft = hardwareMap.get(DcMotor.class,"backLeft");
        backRight = hardwareMap.get(DcMotor.class,"backRight");

        launchMechRight = hardwareMap.get(DcMotor.class,"launchMechRight");
        //launchMechLeft = hardwareMap.get(DcMotor.class,"launchMechLeft");
        dispenserMech = hardwareMap.servo.get("dispenserMech");

        launchMechRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //launchMechLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //caption: what appears on the phone
        loggingTlm = telemetry.addData("distance traveled", "unknown");

        //initializing drivetrain
        drivetrain1 = new MechanumGearedDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain1.resetEncoders();
        drivetrain1.encodersOn();
        RobotLog.i("start moving");

        //initializing gamepad variables
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        //initializing autonomous path
        initPath();

        //initialize powershot timer
        // but this method was taken out, just in case it is here
        //startPowerShotTimer();
    }


//    public void startStrafing()
//    {
//        //start looking for Skystones
//        RobotLog.i("startStrafing");
//        addTask(sdTask);
//        loggingTlm.setValue("startStrafing:before starting to strafe");
//        if (allianceColor == AllianceColor.RED) {
//            drivetrain1.strafe(opmodes.SkyStoneConstants25.STRAFE_SPEED);
//        } else {
//            drivetrain1.strafe(-opmodes.SkyStoneConstants25.STRAFE_SPEED);
//        }
//        loggingTlm.setValue("startStrafing:after starting to strafe");
//    }


    @Override
    public void start()
    {
        loggingTlm = telemetry.addData("log", "unknown");
        parkOnLaunchLine();
        //only need to say this because it will lead into the next methods
    }
}
