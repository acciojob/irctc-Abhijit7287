package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        //Incase the there are insufficient tickets
        //throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Optional<Train> trainObj = trainRepository.findById(bookTicketEntryDto.getTrainId());

        Train train = trainObj.get();

        String s = train.getRoute();

        String[]str = s.split(",");

        int a = -1 ,b =-1;

        for(int i=0;i<str.length;i++){
            String ss = str[i];
            if(ss.equals(bookTicketEntryDto.getFromStation().toString())){
                a = i;
            }
            if(ss.equals(bookTicketEntryDto.getToStation().toString())){
                b = i;
            }
        }

        if(b==-1 || a==-1 || b<a) {
            throw new Exception("Invalid stations");
        }

        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto(bookTicketEntryDto.getTrainId()
                ,bookTicketEntryDto.getFromStation(),bookTicketEntryDto.getToStation());


        int check = calculateAvailableSeats(seatAvailabilityEntryDto);

       if(check<bookTicketEntryDto.getNoOfSeats()){
           throw new Exception("Less tickets are available");
       }


       int toIndex =0 , fromIndex = 0;

        for(int i=0;i<str.length;i++){
            String s1 = str[i];
           if(s1.equals(bookTicketEntryDto.getFromStation().toString())){
               fromIndex = i;
           }
           if(s1.equals(bookTicketEntryDto.getToStation().toString())){
               toIndex = i;
           }
        }

        int multi  = toIndex-fromIndex;

        int totalFare = (300*multi) * bookTicketEntryDto.getNoOfSeats();

        ////setting the passenger
        List<Integer> list = bookTicketEntryDto.getPassengerIds();

        Ticket ticket = new Ticket();

        for(int id : list){
            Passenger passenger = passengerRepository.findById(id).get();

            ticket.getPassengersList().add(passenger);
        }

        ///setting other attributes
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFare);


        ///setting foreign key
        ticket.setTrain(train);

        List<Ticket> tickets = train.getBookedTickets();

        tickets.add(ticket);

        train.setBookedTickets(tickets);

        ///setting booking person
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        passenger.getBookedTickets().add(ticket);

        ///saving the ticket for ticket id
        Ticket ticket1 = ticketRepository.save(ticket);


        return ticket1.getTicketId();

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

}
