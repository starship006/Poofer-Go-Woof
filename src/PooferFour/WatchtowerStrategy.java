package PooferFour;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class WatchtowerStrategy {

    //TODO make watchtowers work!
    //called for every Watchtower every turn
    static void runWatchtower(RobotController rc) throws GameActionException {
        //Watchtower code

        //check to see if theres people around, attack first sensed enemy
        RobotInfo[] enemyRobots = rc.senseNearbyRobots();
        for (RobotInfo enemy : enemyRobots){
            if(enemy.team == rc.getTeam().opponent()){
                if(rc.canAttack(enemy.location)){
                    rc.attack(enemy.location);
                    break;
                }
            }
        }

    }
}
