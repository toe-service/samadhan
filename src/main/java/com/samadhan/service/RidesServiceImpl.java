package com.samadhan.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.samadhan.entity.User;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.exception.SamadhanException;
import com.samadhan.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Ride;
import com.samadhan.repository.RidesRepository;

@Service
public class RidesServiceImpl implements RidesService {

	private static final double EARTH_RADIUS = 6371000;
    private static final Logger logger = LoggerFactory.getLogger(RidesServiceImpl.class);

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	@Autowired
	RidesRepository rideRepo;

	@Autowired
	UserRepository userRepo;

	@Override
	public Ride popupRemove(Long rideId, Long userId, Long driverId) {

		Ride rides = rideRepo.findByStatus(rideId, userId, driverId);

		return rides;
	}

	@Override
	public List<Ride> getRidesByuser(Long userId) {


		List<Ride> ridesByUserId = rideRepo.findByUserId(userId);
		System.out.println("ridesByUserId" + ridesByUserId);
		return ridesByUserId;

//		return null;
	}

	@Override
	public Ride rideStartEnd(Long userId,
							 Boolean rideFlag,
							 Long driverId,
							 int otp,
							 String rideId) throws SamadhanException {
		//rideFlag 1 is for start
		//if (rideFlag) {
		try {
//			boolean canAccept = isWithin100Meters(driverLatitude, driverLongitude, sourceLatitude, sourceLongitude);
//
//			if (canAccept) {
//				System.out.println("✅ Driver is within 100 meters. Accept ride.");
//			} else {
//				System.out.println("❌ Driver is too far.");
//			}
		//	Optional<Ride> ride = rideRepo.findById(rideId);
			Ride ridepresent = rideRepo.existRide(rideId, userId);

		//	Ride ridepresent = ride.get();
			int rideStatus;
			if(rideFlag) {
				rideStatus= rideStatusEnum.ONGOING.getId();
			} else {
				rideStatus = rideStatusEnum.COMPLETED.getId();
			}

			ridepresent.setRideStatus(rideStatus);

			rideRepo.save(ridepresent);

			Ride getride = new Ride();
			if (ridepresent.getRideOtp() != otp) {
				throw new SamadhanException("Wrong otp");
			}


			//}

			return ridepresent;

//		}catch(Exception e){
//			throw new SamadhanException(e);
//		}

		}catch (Exception e) {
			throw e; // rethrow as-is
		}
//		catch (Exception e) {
//			throw new SamadhanException("Unexpected error: " + e.getMessage(), e);
//		}


//	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//		double dLat = Math.toRadians(lat2 - lat1);
//		double dLon = Math.toRadians(lon2 - lon1);
//
//		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//						Math.sin(dLon / 2) * Math.sin(dLon / 2);
//
//		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//
//		return EARTH_RADIUS * c; // distance in meters
//	}
//
//	public static boolean isWithin100Meters(double driverLat, double driverLon,
//											double userLat, double userLon) {
//		double distance = calculateDistance(driverLat, driverLon, userLat, userLon);
//		return distance <= 100;  // check if within 100 meters
//	}


	}

	@Override
	public int generateOtp() {

		int n = SECURE_RANDOM.nextInt(1_000_0);
		System.out.println("n"+n);
		return n;

		//return 0;
	}

	@Override
	public String generateRideId(Long userId) {

		Optional<User> user=userRepo.findById(userId);
		String userName=user.get().getUserName();

		String initials = userName.substring(0, 2).toUpperCase();

		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

		// Time in 24hr format HHmm
		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));

		Random random = new Random();
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		StringBuilder sb = new StringBuilder();
		sb.append(initials).append(date).append(time); // fixed 12 chars

		for (int j = 0; j < 8; j++) {
			sb.append(characters.charAt(random.nextInt(characters.length())));
		}

		String randomRideId=sb.toString();

		return randomRideId;
	}

    @Override
    public Ride getRideByRideId(String rideId) {
        try {
            return rideRepo.findByRideId(rideId);
        } catch (Exception exp) {
            logger.error("error occurred while fetching ride by ride id and error is {}", exp.getCause());
            throw exp;
        }
    }
}


