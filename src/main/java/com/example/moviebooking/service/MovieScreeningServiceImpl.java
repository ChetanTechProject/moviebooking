package com.example.moviebooking.service;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebooking.dao.ScreenRepo;
import com.example.moviebooking.dao.ScreeningRepo;
import com.example.moviebooking.dao.SeatRepo;
import com.example.moviebooking.dao.TheatreRepo;
import com.example.moviebooking.data.dto.BookSeatsDto;
import com.example.moviebooking.data.entity.Screening;
import com.example.moviebooking.data.entity.Seat;
import com.example.moviebooking.data.entity.Theatre;
import com.example.moviebooking.domain.MovieScreening;
import com.example.moviebooking.util.DateUtil;

@Service
public class MovieScreeningServiceImpl implements MovieScreeningService {

	Logger log = LoggerFactory.getLogger(MovieScreeningServiceImpl.class);

	@Value("${moviescreeningdetails}")
	private String getmoviescreeningdetails;

	@PersistenceContext
	private EntityManager entityMgr;

	@Autowired
	private ScreeningRepo screeningRepository;

	@Autowired
	private ScreenRepo screenRepository;

	@Autowired
	private TheatreRepo theatreRepository;
	
	@Autowired
	private SeatRepo seatRepository;

	@Override
	public List<MovieScreening> getAllMovieDetails(String movieName, String city, String screeningDt) {

		Query q = entityMgr.createNativeQuery(getmoviescreeningdetails);
		q.setParameter(1, movieName);
		q.setParameter(2, city);
		q.setParameter(3, screeningDt);

		List resultList = q.getResultList();

		MovieScreening movieScreening = null;
		List<MovieScreening> movieList = new ArrayList<MovieScreening>();
		BigInteger ingr = null;

		for (Object obj : resultList) {
			movieScreening = new MovieScreening();
			Object[] objArray = (Object[]) obj;
			movieScreening.setTheatreName((String) objArray[0]);
			movieScreening.setTheatreCity(city);
			ingr = (BigInteger) objArray[1];
			movieScreening.setScreenId(ingr.intValue());
			movieScreening.setMovieName(movieName);
			movieScreening.setScreeningDate(screeningDt);
			movieScreening.setScreeningTime(DateUtil.convertTimeToString((java.sql.Time) objArray[2]));
			movieScreening.setBookedSeats((int) objArray[3]);
			movieList.add(movieScreening);

		}

		return movieList;
	}

	@Override
	@Transactional
	public int bookSeats(BookSeatsDto bookSeatsDto, int seats) throws Exception{
		final String COLON_DELIMETER = ":";
		List<Seat> preferredSeatList = new ArrayList<Seat>();
		Seat prefSeatEntity = null;
		char rowId ;
		int seatNbr = -1;

		Screening screening = getScreening(bookSeatsDto);
		screening.setBookedTickets(seats);
		//save screening
		screeningRepository.save(screening);
		log.info("Screening Info Saved ");
		//save preferred seats
		List<String> preferredSeats = bookSeatsDto.getPreferredSeats();
		long screenId = screening.getScreenId();
		for(String prefSeat : preferredSeats) {
			String[]seatArray = prefSeat.split(COLON_DELIMETER);
			if(seatArray != null && seatArray.length > 0) {
				prefSeatEntity = new Seat();
				rowId = seatArray[0].charAt(0);
				seatNbr = Integer.valueOf(seatArray[1]);
				log.info("Saving Row id " + rowId  + " seat number " + seatNbr);
				prefSeatEntity.setRowId(rowId);
				prefSeatEntity.setSeatNbr(seatNbr);
				prefSeatEntity.setScreenId(screenId);
				preferredSeatList.add(prefSeatEntity);
				}
		}
		
		seatRepository.saveAll(preferredSeatList);
		log.info("Preferred Seat Details saved.  ");
		return getBookedSeats(bookSeatsDto);
	}

	@Override
	public int getBookedSeats(BookSeatsDto bookSeatsDto) {
		Screening screening = getScreening(bookSeatsDto);
		return screening.getBookedTickets();
	}

	@Override
	public int getTotalSeats(BookSeatsDto bookSeatsDto) {
		return screenRepository.findByScreenId(bookSeatsDto.getScreenId()).getSeatsNum();
	}

	private Screening getScreening(BookSeatsDto bookSeatsDto) {
		Theatre theatre = theatreRepository.findByTheatreNameAndTheatreCity(bookSeatsDto.getTheatreName(),
				bookSeatsDto.getTheatreCity());
		if (theatre == null)
			return null;

		java.sql.Time time = null;
		log.info("Converted Date " + java.sql.Date.valueOf(bookSeatsDto.getScreeningDate()));
		try {
			time = DateUtil.convertStringToTime(bookSeatsDto.getScreeningTime());
			log.info("Converted Time " + time);
		} catch (ParseException pe) {
			log.error(pe.getMessage());
			return null;
		}

		return screeningRepository.findByMovieNameAndTheatreIdAndScreeningDateAndScreeningTime(
				bookSeatsDto.getMovieName(), theatre.getTheatreId(),
				java.sql.Date.valueOf(bookSeatsDto.getScreeningDate()), time);
	}

}
