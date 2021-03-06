/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package net.imglib2.algorithm.morphology;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ij.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.CenteredRectangleShape;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.list.ListCursor;
import net.imglib2.img.list.ListImg;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class MorphologyOperationsTest {

	public static void main(final String[] args) {
		weirdTypeTest();
		standardDilate();
		standardErode();
		openAndClose();
	}

	public static void openAndClose() {
		final ArrayImg<UnsignedByteType, ByteArray> img = ArrayImgs.unsignedBytes(new long[] { 50, 50 });
		final Random ran = new Random();
		for (final UnsignedByteType pixel : img) {
			if (ran.nextDouble() > 0.95)
				pixel.set(255);
		}

		final Shape strel = new HyperSphereShape(3);
		final Img< UnsignedByteType > open = Opening.open( img, strel, 4 );
		final Img< UnsignedByteType > closed = Closing.close( img, strel, 4 );

		new ImageJ();
		ImageJFunctions.show(img);
		ImageJFunctions.show(open);
		ImageJFunctions.show(closed);
	}

	public static void standardErode() {
		final ArrayImg<UnsignedByteType, ByteArray> img = ArrayImgs.unsignedBytes(new long[] { 10, 9 });
		for (final UnsignedByteType pixel : img) {
			pixel.set(255);
		}
		final ArrayRandomAccess<UnsignedByteType> ra = img.randomAccess();
		ra.setPosition(new int[] { 1, 4 });
		ra.get().set(0);

		final Shape strel = new CenteredRectangleShape(new int[] { 3, 2 }, false);
		// final Shape strel = new HyperSphereShape(radius)
		final Img< UnsignedByteType > full = Erosion.erodeFull( img, strel, 4 );
		final Img< UnsignedByteType > std = Erosion.erode( img, strel, 4 );

		new ImageJ();
		ImageJFunctions.show(img);
		ImageJFunctions.show(full);
		ImageJFunctions.show(std);
	}

	public static void standardDilate() {
		final ArrayImg<UnsignedByteType, ByteArray> img = ArrayImgs.unsignedBytes(new long[] { 3, 9 });
		final ArrayRandomAccess<UnsignedByteType> ra = img.randomAccess();
		ra.setPosition(new int[] { 1, 4 });
		ra.get().set(255);

		final Shape strel = new CenteredRectangleShape(new int[] { 5, 2 }, true);
		// final Shape strel = new HyperSphereShape(radius)
		final Img< UnsignedByteType > full = Dilation.dilateFull( img, strel, new UnsignedByteType( 0 ), 4 );
		final Img< UnsignedByteType > std = Dilation.dilate( img, strel, new UnsignedByteType( 0 ), 4 );

		new ImageJ();
		ImageJFunctions.show(img);
		ImageJFunctions.show(full);
		ImageJFunctions.show(std);
	}

	public static void weirdTypeTest() {

		/*
		 * The 35 words added by the Oxford Online Dictionary during summer
		 * 2012. And another one.
		 */
		final String[] wordArray = new String[] { "Bling", "Bromance", "Chillax", "Crunk", "D'oh", "Droolworthy", "Frankenfood", "Grrrl", "Guyliner", "Hater", "Illiterati", "Infomania", "Jeggings", "La-la Land", "Locavore", "Mankini", "Mini-Me", "Muffin Top", "Muggle", "Noob", "Obvs", "OMG", "Po-po", "Purple State", "Screenager", "Sexting", "Textspeak", "Totes", "Truthiness", "Twitterati", "Unfriend", "Upcycle", "Whatevs", "Whovian", "Woot", "Jean-Yves" };
		final List<String> words = Arrays.asList(wordArray);
		Collections.shuffle(words);

		final ListImg<StringType> img = new ListImgFactory<StringType>().create(new long[] { 6l, 6l }, new StringType());
		final ListCursor<StringType> cursor = img.cursor();
		for (final String str : words) {
			cursor.fwd();
			cursor.get().set(str);
		}

		System.out.println("Before:\n" + toString(img, 14));
		System.out.println();
		final Img< StringType > dilatedFull = Dilation.dilateFull( img, new RectangleShape( 1, false ), new StringType( "" ), 2 );
		System.out.println("After full:\n" + toString(dilatedFull, 14));
		System.out.println();
		final Img< StringType > dilated = Dilation.dilate( img, new RectangleShape( 1, false ), new StringType( "" ), 2 );
		System.out.println("After std:\n" + toString(dilated, 14));

	}

	public static final <T> String toString(final Img<T> img, final int columnSize) {
		final StringBuilder str = new StringBuilder();
		final long maxX = img.dimension(0);
		final long maxY = img.dimension(1);

		final RandomAccess<T> ra = img.randomAccess();
		for (long y = 0; y < maxY; y++) {
			ra.setPosition(y, 1);
			for (long x = 0; x < maxX; x++) {
				ra.setPosition(x, 0);
				final String s = ra.get().toString();
				str.append(s);
				for (int i = 0; i < columnSize - s.length(); i++) {
					str.append(' ');
				}
			}
			str.append('\n');
		}

		return str.toString();
	}

	public static class StringType implements Type<StringType>, Comparable<StringType> {

		private String str;

		public StringType() {}

		public StringType(final String str) {
			this.str = str;
		}

		@Override
		public StringType createVariable() {
			return new StringType();
		}

		@Override
		public StringType copy() {
			return new StringType(str);
		}

		@Override
		public void set(final StringType c) {
			str = c.str;
		}

		public void set(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public int compareTo(final StringType o) {
			return str.compareToIgnoreCase(o.str);
		}

		@Override
		public boolean valueEquals( StringType t )
		{
			return str.equals( t.str );
		}

	}
}
