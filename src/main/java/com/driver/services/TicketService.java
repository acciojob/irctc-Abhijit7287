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

    @Autowired
    TrainService trainService;


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

        int a = 0 ,b =0;

        for(String ss : str){
            if(ss.equals(bookTicketEntryDto.getFromStation())){
                a = 1;
            }
            if(ss.equals(bookTicketEntryDto.getToStation())){
                b=1;
            }
        }

        if(b==0 && a==0){
            throw new Exception("Invalid stations");
        }

        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto(bookTicketEntryDto.getTrainId()
                ,bookTicketEntryDto.getFromStation(),bookTicketEntryDto.getToStation());


       int check = trainService.calculateAvailableSeats(seatAvailabilityEntryDto);

       if(check<bookTicketEntryDto.getNoOfSeats()){
           throw new Exception("Less tickets are available");
       }


       int toIndex =0 , fromIndex = 0;

        for(int i=0;i<str.length;i++){
            String s1 = str[i];
           if(s1.equals(bookTicketEntryDto.getFromStation())){
               fromIndex = i;
           }
           if(s1.equals(bookTicketEntryDto.getToStation())){
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
}
