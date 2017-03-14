package templar.atakr.DatabaseObjects;

/**
 * Created by Devin on 3/9/2017.
 */

public class Video {
    private static final String TAG = Video.class.getSimpleName();

    private String youtubeVideoId;
    private String youtubeUrl;
    private String youtubeName;
    private String atakrName;
    private String uploader;
    //private String creator;
    private float views;
    private String youtubeThumbailUrl;
    private float popularity; // views / (current time - time uploaded) = views per time unit (in ms)
    private long timeUploaded;

    public Video(){
    }

    public Video(String id, String url, String yName, String aName, String uploadedBy, String yUrl, String v){
        youtubeVideoId = id;
        youtubeUrl = url;
        youtubeName = yName;
        atakrName = aName;
        uploader = uploadedBy;
        youtubeThumbailUrl = yUrl;
        views = Float.valueOf(v);
        timeUploaded = System.currentTimeMillis();
        calculatePopularity();
    }

    private void calculatePopularity(){
        if(System.currentTimeMillis() - timeUploaded > 0 && views > 0) {
            popularity = views / (System.currentTimeMillis() - timeUploaded);
        }else{
            popularity = 0;
        }
    }

    public String getYoutubeVideoId(){ return youtubeVideoId;}
    public void setYoutubeVideoId(String s){ youtubeVideoId = s;}

    public String getYoutubeUrl(){return youtubeUrl;}
    public void setYoutubeUrl(String s){
        youtubeUrl = s;}

    public String getYoutubeName(){return youtubeName;}
    public void setYoutubeName(String s){youtubeName = s;}

    public String getAtakrName(){return atakrName;}
    public void setAtakrName(String s){atakrName = s;}

    public String getUploader(){return uploader;}
    public void setUploader(String s){uploader = s;}

    public float getViews(){return views;}
    public void setViews(Float f){views = f;}

    public String getYoutubeThumbailUrl(){return youtubeThumbailUrl;}
    public void setYoutubeThumbailUrl(String yUrl){youtubeThumbailUrl = yUrl;}

    public float getPopularity(){return popularity;}
    public void setPopularity(Float f){popularity = f;}

    public long getTimeUploaded(){return timeUploaded;}
    public void setTimeUploaded(Long l){timeUploaded = l;}
}