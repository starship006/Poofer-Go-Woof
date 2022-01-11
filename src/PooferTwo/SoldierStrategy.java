package PooferTwo;

import battlecode.common.*;
// Java code to illustrate String
import java.io.*;
import java.lang.*;

public class SoldierStrategy {
    //called for every Soldier every turn

    static final int AMOUNT_OF_ENEMIES_TO_BE_A_TARGET = SharedConstants.AMOUNT_OF_ENEMIES_TO_BE_A_TARGET;
    static final int AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET = SharedConstants.AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET;
    static Direction exploreDir = null; //
    static MapLocation targetLocation = null;
    static String state = "exploring";

    static boolean IS_SMALL = false; //HARD CODE MORE STUFF FOR SMALL MAP ! ! !

    //fighting variables




    static void runSoldier(RobotController rc) throws GameActionException {
        //Soldier Find Danger Mode
        //init code
        if(exploreDir == null){
            GenerateExplorationDirection(rc);
        }

        //sense environment
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);

        RobotInfo lowestHealthEnemy = null;
        int lowestHealth = 100000;
        for (RobotInfo enemy : enemies){
            if (enemy.getHealth() < lowestHealth){
                lowestHealthEnemy = enemy;
                lowestHealth = enemy.getHealth();
            }
            if (enemy.getType() == RobotType.ARCHON){
                rc.writeSharedArray(0,2); //signal archon to read
                rc.writeSharedArray(4, enemies[0].location.y * 100 + enemies[0].location.x); //signal enemy archon location
            }
        }
        if (enemies.length > AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET - 1){
            rc.writeSharedArray(3, rc.getLocation().y * 100 + rc.getLocation().x); //signal archon to read

        }


        //set states
        if (lowestHealthEnemy != null){
            rc.writeSharedArray(2,lowestHealthEnemy.getLocation().y*100 + lowestHealthEnemy.getLocation().x);
            state = "battling";
        }else{
            if (targetLocation != null){
                state = "targeting";
            } else{
                state = "exploring";
            }
        }

        //execute states
        if (state.equals("exploring")){
            MovementOptions.pathTowards(rc,rc.getLocation().add(exploreDir));
        }else if(state.equals("battling")){
            fightingCode(rc, lowestHealthEnemy);
        }else if(state.equals("targeting")){
            MovementOptions.pathTowards(rc,targetLocation);
        }


        //read array and update target locations if needed
        if (rc.getRoundNum() % AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET == 0){
            int position = rc.readSharedArray(4); //if there was an archon detected
            if (position > 0){
                targetLocation = new MapLocation(position % 100, position/100);
                rc.setIndicatorString(String.valueOf(position));
            }else{
                position = rc.readSharedArray(3); // if there was enemies detected

                if(position>0){
                    targetLocation = new MapLocation(position % 100, position/100);
                    rc.setIndicatorString(String.valueOf(position));
                }else{
                    targetLocation = null;
                    GenerateExplorationDirection(rc);
                }
            }
        }
    }

    static void fightingCode(RobotController rc, RobotInfo lowestHealthEnemy) throws GameActionException{



        //attack if theres a lowest enemy

        if (rc.canAttack(lowestHealthEnemy.getLocation())){
            rc.attack(lowestHealthEnemy.getLocation());
        }

        //run closer to the lowest enemy if it exists
        if (rc.isMovementReady()) {
            MovementOptions.pathTowards(rc, lowestHealthEnemy.getLocation());
        }





    }

    static void GenerateExplorationDirection(RobotController rc) throws GameActionException{
        //find quadrant
        int MapXCenter = rc.getMapWidth() / 2;
        int MapYCenter = rc.getMapHeight() / 2;
        int SpawnX = rc.getLocation().x;
        int SpawnY = rc.getLocation().y;

        if(SpawnY >= MapYCenter && SpawnX >= MapXCenter){
            //top right
            exploreDir = Direction.SOUTHWEST;
        }else if(SpawnY >= MapYCenter && SpawnX <= MapXCenter){
            //top left
            exploreDir = Direction.SOUTHEAST;
        }else if(SpawnY <= MapYCenter && SpawnX >= MapXCenter){
            //bottom right
            exploreDir = Direction.NORTHWEST;
        }else{
            //bottom left
            exploreDir = Direction.NORTHEAST;
        }

        RobotPlayer.rng.setSeed(rc.getID());
        int modifier = RobotPlayer.rng.nextInt(3); // 3 options to shift left, right, and stay
        if (modifier == 0 ){
            exploreDir = exploreDir.rotateLeft();
        }else if(modifier == 1){
            exploreDir = exploreDir.rotateRight();
        }
        rc.setIndicatorString(exploreDir.toString());

    }


    //OUT OF USE
    static void otherCode(RobotController rc) throws GameActionException{
        if (rc.getRoundNum() % AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET == 0){
            int position = rc.readSharedArray(3); // if there was enemies detected
            if (position > 0){
                targetLocation = new MapLocation(position % 100, position/100);
            }
            position = rc.readSharedArray(4); //if there was an archon detected
            if (position > 0){
                targetLocation = new MapLocation(position % 100, position/100);
            }
        }

        if (targetLocation != null){
            //pathfind
            MovementOptions.pathTowards(rc, targetLocation);
        }
        if(rc.isMovementReady()){  //this is if the robot is already close enough. now we just explore
            //explore in a direction
            if(rc.canMove(exploreDir)){
                rc.move(exploreDir);
            }else if(!rc.onTheMap(rc.getLocation().add(exploreDir))){
                exploreDir = exploreDir.opposite();
            }else if(rc.isMovementReady()){
                MovementOptions.moveRandomly(rc);
            }
        }



        //try to attack enemy + signal enemy spotted
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0 ) {
            MapLocation toAttack = enemies[0].location;

            if (enemies[0].getType() == RobotType.ARCHON) {
                rc.writeSharedArray(4, enemies[0].location.y * 100 + enemies[0].location.x);
            }

            int PositionNumber = toAttack.y * 100 + toAttack.x;
            rc.writeSharedArray(2, PositionNumber);


            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
                if (IS_SMALL) {
                    targetLocation = enemies[0].location;

                }
            }
        }
    }
}
