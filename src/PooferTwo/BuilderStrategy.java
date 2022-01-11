package PooferTwo;

import battlecode.common.*;

import java.util.Arrays;


public class BuilderStrategy {
    //constants
    static final int DISTANCE_SQUARED_OUT = 49;
    //called for every Builder every turn
    static MapLocation targetLocation;
    static int turnCount = 0;
    static int watchtowerCount = 0;


    static void runBuilder(RobotController rc) throws GameActionException {
        //Builder code

        if (turnCount == 0){
            //establish target location (which, as of now, is towards the enemy located on array index 2)
            int EnemyPosition = rc.readSharedArray(2);
            targetLocation = new MapLocation(EnemyPosition%100, EnemyPosition/100);
        }

        //see distance left towards destination
        int DistanceTo = rc.getLocation().distanceSquaredTo(targetLocation);

        if (DistanceTo > DISTANCE_SQUARED_OUT){
            //move towards enemy if far away
            MovementOptions.pathTowards(rc,targetLocation);
        }else{
            //repair watchtowers if possible
            boolean robotInRadiusThatNeedsRepairs = false;
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(9,rc.getTeam());
            for(RobotInfo robot : nearbyRobots){
                if(robot.getType() == RobotType.WATCHTOWER){
                    if(robot.getHealth() < robot.getType().health){
                        robotInRadiusThatNeedsRepairs = true;
                        if(rc.canRepair(robot.location)){
                            //if there is a watchtower THAT NEEDS HEALING, heal
                            rc.repair(robot.location);
                        }
                    }
                }
            }
            //start building towers if close to enemy and no repairs to be made
            if (watchtowerCount < 3){
                tryBuildWatchtowerOnLowRubble(rc);
            }else if(robotInRadiusThatNeedsRepairs){
                //if still hasn't moved, and there are no watchtowers it can help with, move!!
                MovementOptions.moveRandomly(rc);
            }
        }
        turnCount++;
    }


    static void tryBuildWatchtowerOnLowRubble(RobotController rc) throws GameActionException{
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, (a, b) -> getRubble(rc, a) - getRubble(rc, b));

        for(Direction d: dirs){
            if (rc.canBuildRobot(RobotType.WATCHTOWER,d)){
                rc.buildRobot(RobotType.WATCHTOWER,d);
                watchtowerCount++;
                break;
            }
        }
    }

    static int getRubble(RobotController rc, Direction d){
        try {
            MapLocation loc = rc.getLocation().add(d);
            return rc.senseRubble(loc);
        }catch (GameActionException e){
            e.printStackTrace();
            return 0;
        }
    }
}
