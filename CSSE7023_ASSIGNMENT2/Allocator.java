package railway;

import java.util.*;

public class Allocator {

	/**
	 * This method takes as input a list of the routes that are currently
	 * occupied by trains on the track, and a list of the routes requested by
	 * each of those trains and returns an allocation of routes to trains based
	 * on those inputs.
	 * 
	 * Such a method may be used by a train controller to manage the movement of
	 * trains on the track so that they do not collide. (I.e. if a train has
	 * been allocated to a route, it has permission to travel on that route.)
	 * 
	 * @require occupied != null && requested != null
	 * 
	 *          && !occupied.contains(null)
	 * 
	 *          && !requested.contains(null)
	 * 
	 *          && occupied.size() == requested.size()
	 * 
	 *          && none of the occupied routes intersect
	 * 
	 *          && the routes in the occupied list are non-empty, valid routes
	 * 
	 *          && the routes in the requested list are non-empty, valid routes
	 * 
	 *          && all the routes in occupied and requested are part of the same
	 *          track.
	 * 
	 * @ensure Let N be the number of elements in the occupied list. This method
	 *         returns a list of N routes, where, for each index i satisfying 0
	 *         <= i < N, \result.get(i) is the route allocated to the ith train:
	 *         the train currently occupying route occupied.get(i).
	 * 
	 *         The route allocated to the ith train is the longest prefix of
	 *         requested.get(i) that does not intersect with any of the routes
	 *         currently occupied by any other train, or any of the routes
	 *         \result.get(j) for indices j satisfying 0 <= j < i. (I.e. trains
	 *         with lower indices have higher priority.)
	 * 
	 *         Neither of the two input parameters, the occupied list and the
	 *         requested list, are modified in any way by this method.
	 *
	 * @param occupied
	 *            there are occupied.size() trains on the track, and parameter
	 *            occupied is a list of the routes currently occupied by each of
	 *            those trains. A precondition of this method is that none of
	 *            the occupied routes are null or empty, they are valid routes,
	 *            and that they do not intersect (i.e. no two trains can occupy
	 *            the same location on the track at the same time).
	 * @param requested
	 *            a list of the routes requested by each of the occupied.size()
	 *            trains. A precondition of the method is that occupied.size()
	 *            == requested.size(), and that none of the requested routes are
	 *            null or empty, and that they are valid routes. For index i
	 *            satisfying 0 <= i < requested.size(), requested.get(i) is the
	 *            route requested by the train currently occupying the route
	 *            occupied.get(i).
	 * @return the list of allocated routes.
	 */
	public static List<List<Segment>> allocate(List<List<Segment>> occupied,
			List<List<Segment>> requested) {
		// An empty list to store the allocatedOffsets for each segment in the
		// allocated routes
		List<ArrayList<ArrayList<Integer>>> allocatedOffsets = 
				new ArrayList<ArrayList<ArrayList<Integer>>>();
		allocatedOffsets = getOffsets(requested, occupied);
		removeZeroOffsets(allocatedOffsets);
		// An list of all the allocated routes for occupied.size() trains
		List<List<Segment>> allocator = createSegments(occupied, requested,
				allocatedOffsets);
		return allocator;
	}

	/**
	 * This method takes the list of occupied and requested routes allocates
	 * offsets to each requested segment for every train by performing a series
	 * of checks to determine what the offsets should be. It checks if the
	 * requested segment will intersect with any of the occupied trains and then
	 * assigns start and end offsets to each of the requested segments
	 * accordingly.
	 * 
	 * These offsets will then be used in createSegments to create segments for
	 * the allocated routes.
	 * 
	 * @require occupied != null && requested != null &&
	 *          !occupied.contains(null) && !requested.contains(null) &&
	 *          occupied.size() == requested.size()
	 * @param requested
	 * 			a list of the routes requested by each of the occupied.size()
	 * 			trains
	 * @param occupied
	 * 			a list of routes that are currently occupied by occupied.size()
	 * 			number of trains where occupied.get(i) is the route of the ith
	 * 			train
	 * @return the list of new offsets for each allocated segment
	 */
	private static List<ArrayList<ArrayList<Integer>>> getOffsets(
			List<List<Segment>> requested, List<List<Segment>> occupied) {
		//An empty array that the final allocated offsets will be added to
		List<ArrayList<ArrayList<Integer>>> allocatedOffsets = 
				new ArrayList<ArrayList<ArrayList<Integer>>>();
		//Integers that will hold the final allocated start and end offsets
		Integer allocatedStartOffset = Integer.valueOf(0);
		Integer allocatedEndOffset = Integer.valueOf(0);
		// Loop through trains in requested list to access their requested route
		for (int n = 0; n < requested.size(); n++) {
			// Array to store the list of segment offsets of each allocated 
			// route 
			ArrayList<ArrayList<Integer>> segmentOffsets = 
					new ArrayList<ArrayList<Integer>>();
			// Loop through segments in train[n]'s requested route
			for (int m = 0; m < requested.get(n).size(); m++) {
				// Array to store the list of start and end offsets of each
				// allocated segment in an allocated route
				ArrayList<Integer> offsets = new ArrayList<Integer>();
				allocatedStartOffset = requested.get(n).get(m).getStartOffset();
				allocatedEndOffset = requested.get(n).get(m).getEndOffset();
				// Loop through requested trains prior to the current train
				for (int i = 0; i < n; i++) {
					// Loop through segments in train[i]'s route
					for (int j = 0; j < requested.get(i).size(); j++) {
						// Check if the two segments on the same section
						if (requested.get(i).get(j).getSection()
								.equals(requested.get(n).get(m).getSection())) {
							// Check if the first location of
							// train[n] is intersecting with train[i]'s route
							if (requested.get(i).get(j).contains(requested
									.get(n).get(m).getFirstLocation())) {
								// No new tracks are allocated so they will not
								// need new offsets. These are marked as 0 to
								// be removed later.
								allocatedStartOffset = 0;
								allocatedEndOffset = 0;
							} else if (requested.get(i).get(j)
									.contains(requested.get(n).get(m)
											.getLastLocation())) {
								// Above: Check if the last location of train[n]
								// is in train[i]'s route
								// Below: Check if they are going
								// in opposite directions
								if (!requested.get(i).get(j)
										.getApproachingEndPoint()
										.equals(requested.get(n).get(m)
												.getApproachingEndPoint())) {
									allocatedStartOffset = requested.get(n)
											.get(m).getStartOffset();
									allocatedEndOffset = requested.get(n).get(m)
											.getSection().getLength()
											- requested.get(i).get(j)
													.getEndOffset()
											- 1;
								} else {
									// Trains are going in the same direction
									allocatedStartOffset = requested.get(n)
											.get(m).getStartOffset();
									allocatedEndOffset = requested.get(i).get(j)
											.getStartOffset() - 1;
								}
							}
						} else if (atJunction(requested.get(i).get(j),
								requested.get(n).get(m))) {
							// Check if train[n] and train[i] are at the
							// same junction
							allocatedStartOffset = requested.get(n).get(m)
									.getStartOffset();
							allocatedEndOffset = requested.get(n).get(m)
									.getEndOffset() - 1;
						}
					}
				}
				// Loop through occupied trains after the current train
				for (int r = n + 1; r < occupied.size(); r++) {
					// Loop through segments in train[r]'s route
					for (int q = 0; q < occupied.get(r).size(); q++) {
						// Check if the two segments on the same section
						if (occupied.get(r).get(q).getSection()
								.equals(requested.get(n).get(m).getSection())) {
							// Check if the first location of the train[n]'s
							// segment is intersecting with train[r]'s route
							if (occupied.get(r).get(q).contains(requested.get(n)
									.get(m).getFirstLocation())) {
								// No new tracks are allocated so they will not
								// need new offsets. These are marked as 0 to
								// be removed later.
								allocatedStartOffset = 0;
								allocatedEndOffset = 0;
							} else if (occupied.get(r).get(q).contains(requested
									.get(n).get(m).getLastLocation())) {
								// Check if the last location of the segment is
								// intersecting then check if the trains are
								// going in opposite directions
								if (!occupied.get(r).get(q)
										.getApproachingEndPoint()
										.equals(requested.get(n).get(m)
												.getApproachingEndPoint())) {
									allocatedStartOffset = requested.get(n)
											.get(m).getStartOffset();
									allocatedEndOffset = requested.get(n).get(m)
											.getSection().getLength()
											- occupied.get(r).get(q)
													.getEndOffset()
											- 1;
								} else {
									// Trains are going in the same direction
									allocatedStartOffset = requested.get(n)
											.get(m).getStartOffset();
									allocatedEndOffset = occupied.get(r).get(q)
											.getStartOffset() - 1;
								}
							}
						} else if (atJunction(occupied.get(r).get(q),
								requested.get(n).get(m))) {
							// Check if two segments are at the same junction
							allocatedStartOffset = requested.get(n).get(m)
									.getStartOffset();
							allocatedEndOffset = requested.get(n).get(m)
									.getEndOffset() - 1;
						}
					}
				}
				offsets.add(allocatedStartOffset);
				offsets.add(allocatedEndOffset);
				segmentOffsets.add(offsets);
			}
			allocatedOffsets.add(segmentOffsets);
		}
		return allocatedOffsets;
	}

	/**
	 * This method checks if two segments are at the same junction. This is to
	 * check if two trains will intersect each other at the same junction.
	 * If the occupied segment and requested segment are both at the 
	 * same junction, this method will return true and if not, it will return
	 * false. 
	 * 
	 * @require !occupied.equals(null) && !requested.equals(null)
	 * @param occupied
	 * 			The occupied segment that is currently being checked
	 * @param requested
	 * 			The requested segment by a particular train that is currently
	 * 			being checked against an occupied segment
	 * @return true if both trains are at the same junction and false if they
	 * 			are not at the same junction 
	 */
	private static boolean atJunction(Segment occupied, Segment requested) {
		// Check if segment ends at a Junction
		if (requested.getEndOffset() == requested.getSection().getLength()) {
			if (occupied.getStartOffset() == 0) {
				if (occupied.getDepartingEndPoint().getJunction().equals(
						requested.getApproachingEndPoint().getJunction())) {
					return true;
				}
				;
			} else if (occupied.getEndOffset() == occupied.getSection()
					.getLength()) {
				if (occupied.getApproachingEndPoint().getJunction().equals(
						requested.getApproachingEndPoint().getJunction())) {
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * This method checks through the allocated offsets to determine whether or
	 * not a particular segment has not been allocated. If a segment has not 
	 * been allocated, it will be marked as [0, 0] in the list of allocated
	 * offsets. This method will check for the marker [0, 0] and remove this
	 * element from the list of allocated offsets accordingly.
	 * 
	 * @require offsets != null && !offsets.contains(null)
	 * @param offsets
	 * 			A list of offsets for the allocated segments that correspond 
	 * 			to the allocated routes of each train
	 * @return a list of offsets without the marker [0, 0] 
	 */
	private static List<ArrayList<ArrayList<Integer>>> removeZeroOffsets(
			List<ArrayList<ArrayList<Integer>>> offsets) {
		// An array to store the marker [0, 0]
		ArrayList<Integer> zeroes = new ArrayList<Integer>();
		zeroes.add(0);
		zeroes.add(0);
		for (int i = 0; i < offsets.size(); i++) {
			if (offsets.get(i).contains(zeroes)) {
				offsets.get(i).remove(zeroes);
			}
		}
		return offsets;
	}

	/**
	 * @require occupied != null && requested != null && offsets != null
	 *          !occupied.contains(null) && !requested.contains(null) && 
	 *          !offsets.contains(null) && !offsets.get(i).contains([0,0]) &&
	 *          occupied.size() == requested.size()
	 * @param occupied
	 * 			a list of routes that are currently occupied by occupied.size()
	 * 			number of trains where occupied.get(i) is the route of the ith
	 * 			train
	 * @param requested
	 * 			a list of the routes requested by each of the occupied.size()
	 * 			trains
	 * @param offsets
	 * 			A list of offsets for the allocated segments that correspond 
	 * 			to the allocated routes of each train
	 * @return a list of allocated routes that correspond to each train in 
	 * 			occupied
	 */
	private static List<List<Segment>> createSegments(
			List<List<Segment>> occupied, List<List<Segment>> requested,
			List<ArrayList<ArrayList<Integer>>> offsets) {
		// An empty list to store the final allocated routes
		List<List<Segment>> createdSegments = new ArrayList<List<Segment>>();
		for (int i = 0; i < requested.size(); i++) {
			// An empty array to store the segments for each route
			ArrayList<Segment> segments = new ArrayList<Segment>();
			for (int j = 0; j < offsets.get(i).size(); j++) {
				Segment s = new Segment(requested.get(i).get(j).getSection(),
						requested.get(i).get(j).getDepartingEndPoint(),
						offsets.get(i).get(j).get(0),
						offsets.get(i).get(j).get(1));
				segments.add(s);
			}
			createdSegments.addAll(Arrays.asList(segments));
		}
		return createdSegments;

	}

}
