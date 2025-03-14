package coltExpress;

import org.testng.annotations.Test;
//import static org.junit.Assert.*;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;


public class BanditActionTest {
    Train train = new Train();

    @Test
    public void testDeplacerAction() {
        train.removeAllBanditsForTests();
        Bandit player = new Bandit(1);
        player.setWagon(Train.wagons.get(0).get(1));
        TrainElts initialPosition = player.getPosition();

        Action deplacer = new Deplacer(player, Direction.ARRIERE){
            @Override
            public void execute() {
                super.execute();
            }
        };
        deplacer.execute();

        /** Assert that the player has moved to a new position */
        assertNotNull(initialPosition);
        assertEquals(2, player.getPosition().getWagonNum());
    }

    @Test
    public void testBraquerAction() {
        train.removeAllBanditsForTests();
        Bandit player = new Bandit(1);
        TrainElts wagon = new TrainElts(false, 0);
        Butin loot = new Butin("bourses");
        wagon.placeButin(loot);
        player.setWagon(wagon);

        Action braquer = new Braquer(player){
            @Override
            public void execute() {
                super.execute();
            }
        };
        braquer.execute();

        /** Assert that the player has looted the wagon */
        assertEquals(0, wagon.getButins().size());
        assertTrue(player.getLoots().contains(loot));
    }




    @Test
    public void testTirerAction() {
        train.removeAllBanditsForTests();

        Bandit bandit1 = new Bandit(1);
        Bandit bandit2 = new Bandit(2);
        Bandit bandit3 = new Bandit(3);
        Bandit bandit4 = new Bandit(4);
        bandit1.setWagon(Train.wagons.get(0).get(1));
        bandit2.setWagon(Train.wagons.get(0).get(2));
        bandit4.setWagon(Train.wagons.get(0).get(2));
        bandit3.setWagon(Train.wagons.get(1).get(2));

        // Add loot to bandit1
        Butin loot1 = new Butin("bourses");
        bandit1.addButin(loot1);
        bandit3.addButin(loot1);
        bandit4.addButin(loot1);

        /** Ensure all bandits have loot **/
        assertFalse(bandit1.getLoots().isEmpty());
        assertFalse(bandit3.getLoots().isEmpty());
        assertFalse(bandit4.getLoots().isEmpty());

        /** Tirer direction AVANT and ARRIERE works **/
        Action tirer = new Tirer(bandit2, Direction.AVANT) ;
        tirer.execute();

        // bandit loses loot
        assertTrue(bandit1.getLoots().isEmpty());


        /** Tirer direction HAUT works **/
        Action tirer2 = new Tirer(bandit2, Direction.HAUT);
        tirer2.execute();
        assertTrue(bandit3.getLoots().isEmpty());


        /** Tirer direction BAS works : shoot another bandit inside player's wagon **/

        Action tirer3 = new Tirer(bandit2, Direction.BAS);
        tirer3.execute();
        assertTrue(bandit4.getLoots().isEmpty());


    }

}
