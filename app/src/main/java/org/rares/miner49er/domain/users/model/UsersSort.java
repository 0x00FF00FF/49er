package org.rares.miner49er.domain.users.model;

import lombok.Setter;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersSort {

    private static final String TAG = UsersSort.class.getSimpleName();

    /**
     * <p>
     * Sorts data from an array using
     * one of the preconfigured comparators
     * or a custom one, if supplied using
     * {@link #setComparator(Comparator)}.
     * </p>
     *
     * @param list     list of unsorted data
     * @param sortType sort type, one of the following
     *                 <ul>
     *                 <li>{@link ProjectsInterfaces#SORT_TYPE_ALPHA_NUM}</li>
     *                 <li><strike>{@link ProjectsInterfaces#SORT_TYPE_FAVORITES}</strike></li>
     *                 <li><strike>{@link ProjectsInterfaces#SORT_TYPE_RECENT}</strike></li>
     *                 <li>{@link ProjectsInterfaces#SORT_TYPE_SIMPLE}</li>
     *                 </ul>
     * @return         a new, sorted array list
     */
    public List<UserData> sort(List<UserData> list, int sortType) {
        if (ProjectsInterfaces.SORT_TYPE_ALPHA_NUM == sortType) {
            comparator = alphaNumComparator;
        }
        if (ProjectsInterfaces.SORT_TYPE_SIMPLE == sortType) {
            comparator = simpleComparator;
        }
        return sort(list);
    }

    public List<UserData> sort (List<UserData> list) {
        ArrayList<UserData> sortedArray = new ArrayList<>(list);
        Collections.sort(sortedArray, comparator);
        return sortedArray;
    }


    private Comparator<UserData> simpleComparator = new Comparator<UserData>() {
        @Override
        public int compare(UserData o1, UserData o2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            if (res == 0) {
                res = o1.getName().compareTo(o2.getName());
            }
            return res;
        }
    };

    private Comparator<UserData> alphaNumComparator = new Comparator<UserData>() {
        @Override
        public int compare(UserData o1, UserData o2) {
            return new InternalNumberComparator().compare(
                    o1.getName(),
                    o2.getName());
        }
    };

    class InternalNumberComparator implements Comparator {
        private Pattern splitter = Pattern.compile("(\\d+|\\D+)");

        public int compare(Object o1, Object o2) {
            // I deliberately use the Java 1.4 syntax,
            // all this can be improved with 1.5's generics
            String s1 = (String) o1, s2 = (String) o2;
            // We split each string as runs of number/non-number strings
            ArrayList sa1 = split(s1);
            ArrayList sa2 = split(s2);
            // Nothing or different structure
            if (sa1.size() == 0 || sa1.size() != sa2.size()) {
                // Just compare the original strings
                return s1.compareTo(s2);
            }
            int i = 0;
            String si1 = "";
            String si2 = "";
            // Compare beginning of string
            for (; i < sa1.size(); i++) {
                si1 = (String) sa1.get(i);
                si2 = (String) sa2.get(i);
                if (!si1.equals(si2))
                    break;  // Until we find a difference
            }
            // No difference found?
            if (i == sa1.size())
                return 0; // Same strings!

            // Try to convert the different run of characters to number
            int val1, val2;
            try {
                val1 = Integer.parseInt(si1);
                val2 = Integer.parseInt(si2);
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);  // Strings differ on a non-number
            }

            // Compare remainder of string
            for (i++; i < sa1.size(); i++) {
                si1 = (String) sa1.get(i);
                si2 = (String) sa2.get(i);
                if (!si1.equals(si2)) {
                    return s1.compareTo(s2);  // Strings differ
                }
            }

            // Here, the strings differ only on a number
            return val1 < val2 ? -1 : 1;
        }

        ArrayList<String> split(String s) {
            ArrayList<String> r = new ArrayList<>();
            Matcher matcher = splitter.matcher(s);
            while (matcher.find()) {
                String m = matcher.group(1);
                r.add(m);
            }
            return r;
        }

    }


    @Setter
    private Comparator<UserData> comparator = alphaNumComparator;

}
