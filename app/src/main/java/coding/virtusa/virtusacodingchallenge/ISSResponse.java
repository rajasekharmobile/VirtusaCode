package coding.virtusa.virtusacodingchallenge;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajasekharmittapalli on 1/19/18.
 */

public class ISSResponse {
    @SerializedName("response")
    public List<ISSTimes> mTimings = new ArrayList<ISSTimes>();

    class ISSTimes {
        @SerializedName("duration")
        public String duration;

        @SerializedName("risetime")
        public String risetime;
    }
}
