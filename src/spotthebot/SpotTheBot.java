/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spotthebot;

import twitter4j.JSONException;

/**
 *
 * @author Sokratis
 */
public class SpotTheBot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JSONException {
        Crawler crawl = new Crawler();
        RetweetObserver observe = new RetweetObserver();
        UserTracker track = new UserTracker(observe.getHighlyRTed());
    }
    
}
