#pragma once

#include "Export.h"

#include <string>
#include <memory>

using namespace std;

namespace ARDrawing
{
	class ARDRAWING_API ARDrawingTest
	{
	public:
		static shared_ptr<ARDrawingTest> getInstance();

		~ARDrawingTest();
		void loadTrackerData(unsigned char * data, int size);
		void trackImage();
		void unloadTrackerData();

	private:
		static shared_ptr<ARDrawingTest> instance;

		ARDrawingTest();
	};
}