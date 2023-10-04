package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        StringBuilder sb = new StringBuilder();

        List<Station> list = trainEntryDto.getStationRoute();

        for(int i=0;i<list.size();i++){
             Station s = list.get(i);
             sb.append(s);
             if(i!=list.size()-1){
                 sb.append(",");
             }
        }

        Train train = new Train();

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        ////May give Error
        train.setBookedTickets(new ArrayList<>());
        train.setRoute(sb.toString());

        Train trainObj = trainRepository.save(train);

        return trainObj.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Optional<Train> trainobj = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());

        Train train = trainobj.get();

        String s = train.getRoute();

        String[]arr = s.split(",");

        HashMap<String,Integer> map = new HashMap<>();
        int n = 1;

        for(int i=0;i<arr.length;i++){
            String a1 = arr[i];
            map.put(a1,n);
            n++;
        }

        int totalSeats = train.getNoOfSeats();
        int from = map.get(seatAvailabilityEntryDto.getFromStation().toString());
        int to = map.get(seatAvailabilityEntryDto.getToStation().toString());

        List<Ticket> tickets = train.getBookedTickets();

        for(Ticket ticket : tickets){

            int fromCheck = map.get(ticket.getFromStation().toString());
            int toCheck = map.get(ticket.getToStation().toString());

            int noOfPassenger = ticket.getPassengersList().size();


            if(to>fromCheck && from<toCheck){
                totalSeats-=noOfPassenger;
            }
         }

       return totalSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Optional<Train> trainObj = trainRepository.findById(trainId);

        Train train = trainObj.get();

        String s = train.getRoute();

        String[]arr = s.split(",");
        boolean b = false;

        for(String s1 : arr){
            if(s1.equals(station.toString())){
                b = true;
                break;
            }
        }

        if(b==false){
            throw new Exception("Train is not passing from this station");
        }

        List<Ticket> list = train.getBookedTickets();
        int ans = 0;

        for(Ticket ticket : list){

            if(ticket.getFromStation().equals(station)){

                ans+=ticket.getPassengersList().size();
            }
        }

        return ans;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> trainObj = trainRepository.findById(trainId);

        Train train = trainObj.get();

        List<Ticket> list = train.getBookedTickets();

        int age = Integer.MIN_VALUE;

        for(Ticket ticket : list){

            List<Passenger>passengers = ticket.getPassengersList();

            for(Passenger passenger : passengers){
                age = Math.max(age,passenger.getAge());
            }
        }

        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trainList = trainRepository.findAll();

        List<Integer> ans = new ArrayList<>();

        for(Train train : trainList){

            LocalTime time = train.getDepartureTime();


            String s = train.getRoute();

            String[]arr = s.split(",");

            int index = -1;

            for(int i=0;i<arr.length;i++){
                if(arr[i].equals(station.toString())){
                    index = i;
                    break;
                }
            }

            LocalTime departureTime = train.getDepartureTime().plusHours(index);

            if(!departureTime.isBefore(startTime) && !departureTime.isAfter(endTime)){
                ans.add(train.getTrainId());
            }
        }

        return ans;
    }

}
