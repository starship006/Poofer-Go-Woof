package PooferFour;

import battlecode.common.*;
// Java code to illustrate String


public class SoldierStrategy {
    //called for every Soldier every turn

    static final int AMOUNT_OF_ENEMIES_TO_BE_A_TARGET = SharedConstants.AMOUNT_OF_ENEMIES_TO_BE_A_TARGET;
    static final int AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET = SharedConstants.AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET;
    static Direction exploreDir = null; //
    static MapLocation targetLocation = null;
    static String state = "exploring";

    //informations ending
    static boolean haveInformationNotSent = false;
    static int informationToBeSent = -1;
    static int indexToBeSent = -1;

    //movement self code?
    static int ROUNDS_AT_TARGET_WITH_NO_ENEMIES = 0;
    static int MAX_IDLE_TARGET_ROUNDS = 20;
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
                updateSharedInformation(rc, SharedConstants.ARCHON_SPOTTED_LOCATION_INDEX,enemies[0].location.y * 100 + enemies[0].location.x);
            }
        }
        if (enemies.length > AMOUNT_OF_ENEMIES_TO_BE_A_TARGET - 1){
            //updateSharedInformation(rc,SharedConstants.BIG_DANGER_POSITION_INDEX, rc.getLocation().y * 100 + rc.getLocation().x );   TODO FIGURE OUT THIS
        }


        //set states
        if (lowestHealthEnemy != null){
            rc.writeSharedArray(SharedConstants.DANGER_POSITION_INDEX,lowestHealthEnemy.getLocation().y*100 + lowestHealthEnemy.getLocation().x);
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
            //MovementOptions.newPathTowards(rc, targetLocation);
            MovementOptions.pathTowards(rc,targetLocation);
        }





        //check rounds is to reset it if needed
        if (rc.getRoundNum() % AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET == 0 || rc.getRoundNum() % SharedConstants.INDICATOR_SOLDIER == 0){
            int positionValue = rc.readSharedArray(SharedConstants.TROOP_MOVEMENT_LOCATION);
            if (positionValue > 0){
                targetLocation = new MapLocation(positionValue % 100, positionValue/100);
                rc.setIndicatorString(targetLocation.toString());

            }else{
                //this needs to be a signal for something TODO
                targetLocation = new MapLocation(rc.readSharedArray(2)% 100, rc.readSharedArray(2)/100); ; //set a random direction for the miner to explore
                rc.setIndicatorString(targetLocation.toString());

                //rc.setIndicatorString(exploreDir.toString());

            }
            /*/
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
            }/*/
        }

        //send information if have not yet
        if (haveInformationNotSent){
            trySendInformation(rc);
        }
    }

    static void updateSharedInformation(RobotController rc, int index, int information) throws GameActionException{
        haveInformationNotSent = true;
        if (rc.readSharedArray(SharedConstants.LAST_SUBMITTED_INFORMATION_INDEX) != rc.getRoundNum()){
            rc.writeSharedArray(SharedConstants.LAST_SUBMITTED_INFORMATION_INDEX, rc.getRoundNum());
            rc.writeSharedArray(index,information);
            haveInformationNotSent = false;
            informationToBeSent = -1;
            indexToBeSent = -1;
        }else{
            informationToBeSent = information;
            indexToBeSent = index;

        }
    }

    static void trySendInformation(RobotController rc) throws GameActionException{
        if (rc.readSharedArray(SharedConstants.LAST_SUBMITTED_INFORMATION_INDEX) != rc.getRoundNum()){
            rc.writeSharedArray(SharedConstants.LAST_SUBMITTED_INFORMATION_INDEX, rc.getRoundNum());
            rc.writeSharedArray(indexToBeSent,informationToBeSent);
            haveInformationNotSent = false;
            informationToBeSent = -1;
            indexToBeSent = -1;
        }
    }


    static void fightingCode(RobotController rc, RobotInfo lowestHealthEnemy) throws GameActionException{



        //attack if theres a lowest enemy

        if (rc.canAttack(lowestHealthEnemy.getLocation())){
            rc.attack(lowestHealthEnemy.getLocation());
        }

        //run closer to the lowest enemy if it exists
        if (rc.isMovementReady()) {
            boolean CanMove = MovementOptions.pathTowards(rc, lowestHealthEnemy.getLocation());
            if(!CanMove){
                ROUNDS_AT_TARGET_WITH_NO_ENEMIES++;
                if (ROUNDS_AT_TARGET_WITH_NO_ENEMIES > MAX_IDLE_TARGET_ROUNDS){
                    targetLocation = null;
                    ROUNDS_AT_TARGET_WITH_NO_ENEMIES = 0;
                }
            }
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
}
