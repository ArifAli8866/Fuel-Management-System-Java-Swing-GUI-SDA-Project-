package service;

import model.FuelRequest;
import java.util.ArrayList;

public class FuelService {

    public static ArrayList<FuelRequest> requests = new ArrayList<>();

    public static void addRequest(FuelRequest r) {
        requests.add(r);
    }

    public static ArrayList<FuelRequest> getRequests() {
        return requests;
    }

    public static void approveRequest(int id) {
        for (FuelRequest r : requests) {
            if (r.requestId == id) {
                r.status = "Approved";
            }
        }
    }
}