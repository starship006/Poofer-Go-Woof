package PooferFour;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class ArchonStrategy {

    //called for every archon every turn
    static int soldierCount = 0;
    static int sageCount = 0;
    static int minerCount = 0;
    static int builderCount = 0;

    static final int AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET = SharedConstants.AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET; // need to replace later
    static final double RATIO_OF_SOLDIERS_TO_MINERS = 5/3;

    static int stage = 0; //0 early; 1 builder
    static int lastRoundHealth = 1000;

    //communication
    static int lastArchonInformationReceived = 0; //assuming right now that archons don't move
    static int lastEnemyInformationReceived = 0;
    static ArrayList<Integer> possibleArchonLocations = new ArrayList<Integer>();
    static int lastRoundEntered = 0;
    static int enemyLocationOne = 0;
    static int enemyLocationTwo = 0;
    static int enemyLocationThree = 0;

    static void runArchon(RobotController rc) throws GameActionException {
        //archon code
        if (stage==0){
            //if earlygame
            earlyGameActions(rc);
        }else{
            midGameActions(rc);
        }

        //if being rushed
        if(rc.getHealth() != lastRoundHealth){
            lastRoundHealth = rc.getHealth();
            buildOnTopOfLowRubble(rc, RobotType.SOLDIER);
        }


        //communication work

        //read archon updates
        if (rc.readSharedArray(SharedConstants.ARCHON_SPOTTED_LOCATION_INDEX)!=lastArchonInformationReceived){
            possibleArchonLocations.add(rc.readSharedArray(SharedConstants.ARCHON_SPOTTED_LOCATION_INDEX));
            lastArchonInformationReceived = rc.readSharedArray(SharedConstants.ARCHON_SPOTTED_LOCATION_INDEX);
        }

        if((rc.getRoundNum() + 1) % AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET == 0){
            signalBeforeActionCycle(rc);
        }

        if((rc.readSharedArray(SharedConstants.LAST_INDICATED))!=rc.getRoundNum()){
            rc.writeSharedArray(SharedConstants.INDICATOR_INDEX,1); //reset
            System.out.println("Reset Amount");
        }

    }


    //builds a turret in the lowest rubble spot if possible
    static void buildOnTopOfLowRubble(RobotController rc, RobotType type) throws GameActionException {
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, (a, b) -> getRubble(rc, a) - getRubble(rc, b));
        for (Direction d : dirs) {
            if (rc.canBuildRobot(type, d)) {
                rc.buildRobot(type, d);
                switch (type) {
                    case MINER:   minerCount++; break;
                    case SOLDIER: soldierCount++; break;
                    case BUILDER: builderCount++; break;
                    case SAGE: sageCount++; break;
                }
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

    static void earlyGameActions(RobotController rc) throws GameActionException{

        //check if enemies have been spotted and then store that as the direction to send watchtowers
        int EnemyLocation = rc.readSharedArray(2);
        if (EnemyLocation > 0 && builderCount < 2 &&soldierCount > 3 && minerCount > 4){
            //enemy has been spotted
            buildOnTopOfLowRubble(rc, RobotType.BUILDER);
        }

        if (builderCount > 1){
            stage = 1;
        }

        if (minerCount < 5) {
            buildOnTopOfLowRubble(rc, RobotType.MINER);
        }
        else if (soldierCount < 4) {
            buildOnTopOfLowRubble(rc, RobotType.SOLDIER);
        } else if(rc.getHealth() < rc.getType().health){
            //if being rushed
            buildOnTopOfLowRubble(rc, RobotType.SOLDIER);
        }
    }

    static void midGameActions(RobotController rc) throws GameActionException{
    //stage == 1, this is midgame
        if (rc.getTeamLeadAmount(rc.getTeam()) > 180 && RobotPlayer.rng.nextInt(2) % 2 == 1){  //this is a broken way to make it even
        if(builderCount < 2 + rc.getRoundNum() / 800){
            buildOnTopOfLowRubble(rc, RobotType.BUILDER); // send builder every 400
        }
        if (soldierCount  < minerCount * RATIO_OF_SOLDIERS_TO_MINERS){
            buildOnTopOfLowRubble(rc, RobotType.SOLDIER);
        }else{
            buildOnTopOfLowRubble(rc, RobotType.MINER);
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,rc.getTeam().opponent());
        if (enemies.length>0){ //GET BACK ASAP
            createSignalForSoldiersToMove(rc,rc.getLocation().y*100 + rc.getLocation().x);
            System.out.println("SIGNALING FOR HELP");
        }

    }
}

    //robot adding indicator info
    static void createSignalForSoldiersToMove(RobotController rc, int target) throws GameActionException{
        int currentValue = rc.readSharedArray(SharedConstants.INDICATOR_INDEX);
        if (rc.readSharedArray(SharedConstants.LAST_INDICATED) != rc.getRoundNum()) {
            currentValue = 2; //for soldiers
            rc.writeSharedArray(SharedConstants.LAST_INDICATED, rc.getRoundNum());
            //System.out.println("SIGNALED BY SETTING IT TO SOLDEIRS!");
        }

        if (currentValue % SharedConstants.INDICATOR_SOLDIER != 1){
            currentValue = 2;//add soldiers//
            //System.out.println("SIGNALED BY ADDING SOLDEIRS!");
        }

        rc.writeSharedArray(SharedConstants.INDICATOR_INDEX, currentValue);
        rc.writeSharedArray(SharedConstants.TROOP_MOVEMENT_LOCATION, target);
        rc.writeSharedArray(SharedConstants.LAST_INDICATED, rc.getRoundNum());
        System.out.println("new create signal");
        System.out.println(currentValue);
        System.out.println(target);
        System.out.println(rc.getRoundNum());
    }

    static void signalBeforeActionCycle(RobotController rc) throws GameActionException{

        rc.writeSharedArray(SharedConstants.TROOP_MOVEMENT_LOCATION,0);

        if (possibleArchonLocations.size() > 0){
            int lastArchonFound = possibleArchonLocations.get(possibleArchonLocations.size()-1);
            createSignalForSoldiersToMove(rc,lastArchonFound);
            possibleArchonLocations.remove(possibleArchonLocations.size()-1);
        }


        //TODO add code to insert in the robots heading towards danger areas
    }
}
