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

package tests;

import ij.ImageJ;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import net.imglib2.converter.ChannelARGBConverter;
import net.imglib2.display.projector.composite.CompositeXYProjector;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class CompositeXYProjectorBenchmark
{
	final Img< UnsignedByteType > img;

	final Img< ARGBType > argbImg;

	public CompositeXYProjectorBenchmark( final String filename ) throws ImgIOException, IncompatibleTypeException
	{
		// open with ImgOpener using an ArrayImgFactory
		final ArrayImgFactory< UnsignedByteType > factory = new ArrayImgFactory< UnsignedByteType >();
		img = new ImgOpener().openImg( filename, factory, new UnsignedByteType() );
		final long[] dim = new long[ img.numDimensions() - 1 ];
		for ( int d = 0; d < dim.length; ++d )
			dim[ d ] = img.dimension( d );
		argbImg = new ArrayImgFactory< ARGBType >().create( dim, new ARGBType() );
		convert( img, argbImg );

		ImageJFunctions.show( argbImg );
	}

	public void convert( final Img< UnsignedByteType > in,  final Img< ARGBType > out )
	{
		final CompositeXYProjector< UnsignedByteType > projector = new CompositeXYProjector< UnsignedByteType >( in, out, ChannelARGBConverter.converterListRGBA, 2 );
		projector.setComposite( true );
//		projector.setComposite( 0, false );
//		projector.setComposite( 1, true );
//		projector.setComposite( 2, false );
		for ( int iteration = 0; iteration < 10; ++iteration )
		{
			final long start = System.currentTimeMillis();
			for ( int i = 0; i < 10; ++i )
				projector.map();
			final long end = System.currentTimeMillis();
			System.out.println( ( end - start ) + " ms (iteration " + iteration + ")" );
		}
	}

	public static void main( final String[] args ) throws IncompatibleTypeException, ImgIOException
	{
		new ImageJ();
		new CompositeXYProjectorBenchmark( "/home/tobias/Desktop/test.png" );
	}
}
