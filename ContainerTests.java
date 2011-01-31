package tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

import mpicbg.imglib.algorithm.fft.FFTFunctions;
import mpicbg.imglib.algorithm.math.MathLib;
import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.cell.CellContainerFactory;
import mpicbg.imglib.container.imageplus.ImagePlusContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyPeriodicFactory;
import mpicbg.imglib.sampler.PositionableRasterIntervalSampler;
import mpicbg.imglib.sampler.RasterIterator;
import mpicbg.imglib.type.numeric.real.FloatType;

public class ContainerTests
{
	// which dimensions to test
	final long[][] dim = 
		new long[][]{ 
			{ 127 },
			{ 288 },	
			{ 135, 111 },
			{ 172, 131 },
			{  15,  13, 33 },		
			{ 110,  38, 30 },
			{ 109,  34, 111 },
			{  12,  43,  92, 10 },
			{  21,  34,  29, 13 },
			{   5,  12,  30,  4,  21 },
			{  14,  21,  13,  9,  12 }
		}; 
	
	/**
	 * Test only the ArrayContainers
	 */
	@Test public void testArrayContainer()
	{
		for ( int i = 0; i < dim.length; ++i )
		{
			assertTrue( "ArrayContainer failed for: dim=" + MathLib.printCoordinates( dim[ i ] ), 
			            testContainer( dim[ i ], new ArrayContainerFactory(), new ArrayContainerFactory() ) );
		}
	}

	/**
	 * Test CellContainer
	 */
	@Test public void testCellContainer()
	{
		for ( int i = 0; i < dim.length; ++i )
		{
			assertTrue( "ArrayContainer vs CellContainer failed for dim = " + MathLib.printCoordinates( dim[ i ] ),
			            testContainer( dim[ i ], new ArrayContainerFactory(), new CellContainerFactory( 10 ) ) );
			assertTrue( "CellContainer vs ArrayContainer failed for dim = " + MathLib.printCoordinates( dim[ i ] ), 
			            testContainer( dim[ i ], new CellContainerFactory(), new ArrayContainerFactory() ) );
			assertTrue( "CellContainer vs CellContainer failed for dim = " + MathLib.printCoordinates( dim[ i ] ),
			            testContainer( dim[ i ], new CellContainerFactory( 5 ), new CellContainerFactory() ) );
		}
	}

	/**
	 * Test CellContainer
	 */
	@Test public void testImagePlusContainer()
	{
		for ( int i = 0; i < dim.length; ++i )
		{
			if ( dim[ i ].length < 6 )
			{
				assertTrue( "ArrayContainer vs ImagePlusContainer failed for dim = " + MathLib.printCoordinates( dim[ i ] ),
				            testContainer( dim[ i ], new ArrayContainerFactory(), new ImagePlusContainerFactory() ) );
				assertTrue( "ImagePlusContainer vs ArrayContainer failed for dim = " + MathLib.printCoordinates( dim[ i ] ), 
				            testContainer( dim[ i ], new ImagePlusContainerFactory(), new ArrayContainerFactory() ) );
				assertTrue( "ImagePlusContainer vs ImagePlusContainer failed for dim = " + MathLib.printCoordinates( dim[ i ] ),
				            testContainer( dim[ i ], new ImagePlusContainerFactory(), new ImagePlusContainerFactory() ) );
			}
		}
	}
	
	/**
	 * Test MultiThreading
	 */
	@Test public void testMultiThreading()
	{
		assertTrue( "ArrayContainer MultiThreading failed", testThreading( new ArrayContainerFactory() ) );
		assertTrue( "CellContainer MultiThreading failed", testThreading( new CellContainerFactory() ) );
		assertTrue( "ImagePlusContainer MultiThreading failed", testThreading( new ImagePlusContainerFactory() ) );	
	}
	
	protected boolean testThreading( final ContainerFactory factory )
	{
		// create the image
		final Container< FloatType, ? > img = factory.create( new long[]{ 101, 99, 301 }, new FloatType() );

		// get a reference to compare to
		final float[] reference = createReference( img );
		
		// swap quadrants forth and back
		FFTFunctions.rearrangeFFTQuadrants( img, 8 );
		FFTFunctions.rearrangeFFTQuadrants( img, 8 );
		
		boolean succesful = test( img, reference );
		
		return succesful;
	}
	
	protected float[] createReference( final Container<FloatType,?> img )
	{
		// use a random number generator
		final Random rnd = new Random( 1241234 );
		
		// create reference array
		final float[] reference = new float[ ( int )img.size() ];
		
		// iterate over image and reference array and fill with data
		final RasterIterator<FloatType> cursor = img.iterator();			
		int i = 0;
		
		while( cursor.hasNext() )
		{
			cursor.fwd();

			final float value = rnd.nextFloat();
			reference[ i++ ] = value;
			cursor.get().set( value );
		}
		
		return reference;
	}
	
	protected boolean test( final Container< FloatType, ? > img, final float[] reference )
	{
		boolean allEqual = true;
		
		final RasterIterator< FloatType > cursor = img.iterator();
		int i = 0;
		
		while( cursor.hasNext() )
		{
			cursor.fwd();				
			allEqual &= cursor.get().get() == reference[ i++ ];
		}		
		
		return allEqual;
	}
	
	protected boolean testContainer( final long[] size, final ContainerFactory factory1, final ContainerFactory factory2 )
	{
		// create the image
		final Container< FloatType, ? > img1 = factory1.create( size, new FloatType() );
		final Container< FloatType, ? > img2 = factory2.create( size, new FloatType() );
	
		final int numDimensions = img1.numDimensions();

		// get a reference to compare to
		final float[] reference = createReference( img1 );
		
		// copy into a second image using simple cursors
		final RasterIterator<FloatType> cursor1 = img1.iterator();
		final RasterIterator<FloatType> cursor2 = img2.iterator();
		
		while( cursor1.hasNext() )
		{
			cursor1.fwd();
			cursor2.fwd();
			
			cursor2.get().set( cursor1.get() );
		}
		
		cursor1.reset();
		cursor2.reset();
		
		// and copy right back
		while( cursor2.hasNext() )
		{
			cursor1.fwd();
			cursor2.fwd();
			
			cursor1.get().set( cursor2.get() );
		}		

		// copy back into a second image using localizable and positionable cursors			
		final RasterIterator<FloatType> localizableCursor1 = img1.localizingIterator();			
		final PositionableRasterIntervalSampler<FloatType> positionable2 = img2.positionableRasterSampler();			
		
		int i = 0;
		
		while ( localizableCursor1.hasNext() )
		{
			localizableCursor1.fwd();
			++i;
			
			if ( i % 2 == 0 )
				positionable2.moveTo( localizableCursor1 );
			else
				positionable2.setPosition( localizableCursor1 );
			
			positionable2.get().set( localizableCursor1.get() );
		}
		
		// copy again to the first image using a LocalizableByDimOutsideCursor and a LocalizableByDimCursor
		final PositionableRasterIntervalSampler<FloatType> outsideCursor2 = img2.positionableRasterSampler( new OutOfBoundsStrategyPeriodicFactory<FloatType>() );
		localizableCursor1.reset();
		
		final int[] pos = new int[ numDimensions ];			
		i = 0;
		int direction = 1;
		
		try{
			while ( localizableCursor1.hasNext() )
			{
				localizableCursor1.fwd();
				localizableCursor1.localize( pos );
				++i;
				
				// how many times far away from the original image do we grab the pixel
				final int distance = i % 5;
				direction *= -1;				
				
				pos[ i % numDimensions ] += img1.size( i % numDimensions ) * distance * direction;
	
				if ( i % 7 == 0 )
					outsideCursor2.setPosition( pos );
				else
					outsideCursor2.moveTo( pos );
				
				final FloatType t1 = localizableCursor1.get();
				final FloatType t2 = outsideCursor2.get();
				
				final float f1 = t1.getRealFloat();
				final float f2 = t2.getRealFloat();
				
				t1.set( t2 );
				
			}
		}
		catch ( ArrayIndexOutOfBoundsException e ){ System.err.println( ( i % 7 == 0 ? "setPosition() " : "moveTo() " ) + MathLib.printCoordinates( pos ) ); e.printStackTrace(); System.exit( 1 ); }

		final boolean success = test( img1, reference );
		
		return success;
	}
}
