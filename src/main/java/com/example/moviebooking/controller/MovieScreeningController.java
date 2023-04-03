package com.example.moviebooking.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moviebooking.data.dto.BookSeatsDto;
import com.example.moviebooking.data.dto.MovieDetailsDto;
import com.example.moviebooking.domain.MovieScreening;
import com.example.moviebooking.service.MovieScreeningService;

@RequestMapping("/movie/screening")
@RestController
public class MovieScreeningController {

	@Autowired
	private MovieScreeningService movieScreeningService;

	Logger log = LoggerFactory.getLogger(MovieScreeningController.class);

	@CrossOrigin
	@GetMapping("/details")
	public List<MovieScreening> getUiDetails(@RequestBody MovieDetailsDto movieDetailsDto) {
		log.info("Screening details endPoint called");

		if (movieDetailsDto != null) {
			return movieScreeningService.getAllMovieDetails(movieDetailsDto.getMovieName(),
					movieDetailsDto.getMovieCity(), movieDetailsDto.getScreeningDate());
		}

		return null;
	}

	@CrossOrigin
	@PostMapping("/bookseats") 
	public String bookSeats(@RequestBody BookSeatsDto bookSeatsDto) {
	
		log.info(bookSeatsDto.getMovieName());
		log.info(bookSeatsDto.getTheatreCity());
		log.info(bookSeatsDto.getTheatreName());
		log.info(bookSeatsDto.getScreeningTime());
		log.info(bookSeatsDto.getScreeningDate());
		log.info(Integer.toString(bookSeatsDto.getNumSeats()));

		int bookedSeats = this.movieScreeningService.getBookedSeats(bookSeatsDto);
		log.info("Booked Seats " + bookedSeats);
		int totalSeats = this.movieScreeningService.getTotalSeats(bookSeatsDto);
		log.info("Total Seats " + totalSeats);

		if ((bookedSeats + bookSeatsDto.getNumSeats()) > totalSeats)
			return "Sorry " +bookSeatsDto.getNumSeats()+  " Seats Not Avaliable";

		try {
			//append the number of seat's with booked seats to update new booked seats
			this.movieScreeningService.bookSeats(bookSeatsDto, bookedSeats + bookSeatsDto.getNumSeats());
		} catch (Exception e) {
           log.error("Error Saving the Transaction, Please try later! ");
           return "Sorry Unable to Book Seats Currently";
		}

		return bookSeatsDto.getNumSeats() + " Tickets Succesfully booked for Movie " + bookSeatsDto.getMovieName() + "!";
	}

}
