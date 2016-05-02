/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

/**
 *
 * @author sokpa
 */
public class AnnotationTweet {
    String tweetID;
    String text;
    String cleanText;
    String liveFeedLink;
    Integer numTimesPosted;
    
    private void cleanText(){
        String[] split = text.split("\\s+");
            for(int i=0; i<split.length; i++){
                if(split[i].startsWith("@")){ //remove mentions
                    split[i] = "";
                }
                if(split[i].startsWith("http") || split[i].startsWith("www") || split[i].contains("http") || split[i].contains("www")){ //urls
                    split[i] = "";
                }
            }
            String clean="";
            for(int i=0; i<split.length; i++){
                clean += " " + split[i];
            }
            clean = clean.substring(1);
            clean = clean.replaceAll("[\\t\\n\\r]"," ");
            clean = clean.replaceAll("\"", "");
            clean = clean.toLowerCase();
            
            cleanText = clean;
    }
    
    private void calcDirtyText(String t){
        text = t.replaceAll("[\\t\\n\\r]"," ");
        text = text.replaceAll("\"", "");
        text = text.toLowerCase();
    }
    
    private void calcLiveFeedLink(){
        String query = "https://twitter.com/search?q=";
        String[] words = cleanText.split(" ");
        query += words[0];
        for (int i = 1; i < words.length; i++) {
            query += "%20" + words[i];
        }
        query += "&src=typd";
        liveFeedLink = query;
    }
    
    public AnnotationTweet(String id, String text){
        this.tweetID = id;
        calcDirtyText(text);
        cleanText();
        calcLiveFeedLink();
        this.numTimesPosted=0; // it will get 1 when program finds the same tweet in the double iteration
//        System.out.println("Creation!!!=================");
//        System.out.println(this.tweetID);
//        System.out.println(this.text);
//        System.out.println(this.cleanText);
//        System.out.println(this.liveFeedLink);
        
    }

    public String getTweetID() {
        return tweetID;
    }

    public void setTweetID(String tweetID) {
        this.tweetID = tweetID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCleanText() {
        return cleanText;
    }

    public void setCleanText(String cleanText) {
        this.cleanText = cleanText;
    }

    public String getLiveFeedLink() {
        return liveFeedLink;
    }

    public void setLiveFeedLink(String liveFeedLink) {
        this.liveFeedLink = liveFeedLink;
    }

    public Integer getNumTimesPosted() {
        return numTimesPosted;
    }

    public void setNumTimesPosted(Integer numTimesPosted) {
        this.numTimesPosted = numTimesPosted;
    }
    
    public void increaseNumTimesPosted(){
        this.numTimesPosted++;
    }
    
}
