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



        Optional<Train> trainobj = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());

        Train train1 = trainobj.get();

        String ss = train1.getRoute();

        String[]arr = ss.split(",");

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

        List<Ticket> tickets = train1.getBookedTickets();

        for(Ticket ticket : tickets){

            int fromCheck = map.get(ticket.getFromStation().toString());
            int toCheck = map.get(ticket.getToStation().toString());

            int noOfPassenger = ticket.getPassengersList().size();


            if(to>fromCheck && from<toCheck){
                totalSeats-=noOfPassenger;
            }
        }


       if(totalSeats<bookTicketEntryDto.getNoOfSeats()){
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

        List<Passenger> passengers= new ArrayList<>();
        for (int pasangerId: bookTicketEntryDto.getPassengerIds()){
            Passenger passenger= passengerRepository.findById(pasangerId).get();
            passengers.add(passenger);
        }

        Ticket ticket = new Ticket();

        ///setting other attributes
        ticket.setPassengersList(passengers);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFare);


        ///setting foreign key
        ticket.setTrain(train);

        List<Ticket> ticketslist = train.getBookedTickets();

        ticketslist.add(ticket);

        train.setBookedTickets(ticketslist);

        ///setting booking person
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        passenger.getBookedTickets().add(ticket);

        ///saving the ticket for ticket id
        Ticket ticket1 = ticketRepository.save(ticket);


        return ticket1.getTicketId();

    }

}
