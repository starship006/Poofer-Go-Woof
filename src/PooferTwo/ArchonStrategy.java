package PooferTwo;

import battlecode.common.*;

import java.util.Arrays;

public class ArchonStrategy {

    //called for every archon every turn
    static int soldierCount = 0;
    static int sageCount = 0;
    static int minerCount = 0;
    static int builderCount = 0;

    static final int AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET = SharedConstants.AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET; // need to replace later
    static final double RATIO_OF_SOLDIERS_TO_MINERS = 2;

    static int stage = 0; //0 early; 1 builder
    static int lastRoundHealth = 1000;


    static void runArchon(RobotController rc) throws GameActionException {
        //archon code



        if (stage==0){
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

        }else{
            //stage == 1, this is midgame
            if (rc.getTeamLeadAmount(rc.getTeam()) > 180 && RobotPlayer.rng.nextInt(2) % 2 == 1){  //this is a broken way to make it even
                if(builderCount < 2 + rc.getRoundNum() / 400){
                    buildOnTopOfLowRubble(rc, RobotType.BUILDER); // send builder every 400
                }
                if (soldierCount * 4/3 < minerCount){
                    buildOnTopOfLowRubble(rc, RobotType.SOLDIER);
                }else{
                    buildOnTopOfLowRubble(rc, RobotType.MINER);
                }
            }
        }
        if ((rc.getRoundNum() - 1) % AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET == 0){
            rc.writeSharedArray(3,0);
            rc.writeSharedArray(4,0);
        }
        if(rc.getHealth() != lastRoundHealth){
            lastRoundHealth = rc.getHealth();
            buildOnTopOfLowRubble(rc, RobotType.SOLDIER);
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
}
