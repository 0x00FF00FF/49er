package org.rares.miner49er.util;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.ArrayList;
import java.util.List;

public class ModelUtil {
    public static List<UserData> getMatching(List<UserData> users, String userName) {
        List<UserData> filteredUsers = new ArrayList<>();
        List<BoundExtractedResult<UserData>> results = FuzzySearch.extractSorted(userName, users, UserData::getName);
        for (BoundExtractedResult<UserData> result : results) {
            filteredUsers.add(result.getReferent());
        }
        return filteredUsers;
    }

}
