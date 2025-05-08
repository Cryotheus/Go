// Static version of RadixPacked from the cube project
// Why static?
// Because unfortunately classes have overhead an int primitive does not.
// If we don't create a class for a single int, we can save on memory and ptr lookups.

// Everytime an operation is performed, the RADIX of the packed ints must be provided.
// This should be consistent across operations of the same packed int (except splitting and collating ints)

// In this project's use case, a STATE enum is what each digit represents
// since STATE has 3 possible values (0, 1, 2) we would normally bit-pack using 2 bits.
// 2 bits can represent 4 possible values (0, 1, 2, 3)
// so you could say using 2 bits for 3 values is a 25% utililzation loss

// Radix packing lets you pack integers together so they utilize a fractional amount of bits
// so you can expect 4 ints to be packed within exactly 7 bits when using a radix of 3

// I have a theory which would allow utilization of "loose states" in int arrays of packed ints
// but my level of understanding in math is too low
public final class Packing {
	//if we have an array of ints that we are radix packing
	//we will want to find which int in the array holds the digit
	public static int arrayIndex(int radix, int digit) {
		return Math.floorDiv(digit, intCapacity(radix));
	}

	public static int copy(int packed, int radix, int digitSrc, int digitDst, int sourcePacked) {
		return setDigit(packed, radix, digitDst, getDigit(sourcePacked, radix, digitSrc));
	}

	//sets the first `digits` amount of digits to `value`
	public static int fill(int packed, int radix, int digits, int value) {
		//clear the space into which we are adding these digits
		packed = zeroLittle(packed, radix, digits);

		for (int digit = 0; digit < digits; digit++) {
			packed += value * (int) Math.pow(radix, digit);
		}

		//5 * 36 + 5 * 6 + 5
		//+ 5 * 1
		//+ 5 * 6
		//+ 5 * 36
		return packed;
	}

	//shifts the higher-index digits down truncating the lower-index digits
	public static int getBig(int packed, int radix, int digit) {
		return Math.floorDiv(packed, (int) Math.pow(radix, digit));
	}

	//get the lower-index end
	public static int getLittle(int packed, int radix, int digits) {
		return packed % (int) Math.pow(radix, digits + 1);
	}

	//will always return an int [0, radix)
	public static int getDigit(int packed, int radix, int digit) {
		return getBig(packed, radix, digit) % radix;
	}

	//returns the maximum amount of digits of the provided radix a single signed integer can store
	//the parity bit is not utilized
	public static int intCapacity(int radix) {
		return (int) Math.floor(Math.log(2_147_483_647) / Math.log(radix));
	}

	//`value` should always be [0, radix)
	public static int setDigit(int packed, int radix, int digit, int value) {
		//get everything below out digit
		//we need this since we are doing math below that destroys
		//everything below this digit
		//
		//we will add this value back later to recover the lost digits
		int little = getLittle(packed, radix, digit - 1);

		//we get the left part
		//but without our value
		//
		//shift it once to the left
		//so we have the same amount of digits needed when recombining
		//
		//then we add the value of this digit
		//which essentially makes it take the place of the digit we "destroyed"
		int big = getBig(packed, radix, digit + 1) * radix + value;

		return big * ((int) Math.pow(radix, digit)) + little;
	}

	//zeroes the n digits on the lower-index end
	public static int zeroLittle(int packed, int radix, int digits) {
		return getBig(packed, radix, digits) * ((int) Math.pow(radix, digits));
	}
}
