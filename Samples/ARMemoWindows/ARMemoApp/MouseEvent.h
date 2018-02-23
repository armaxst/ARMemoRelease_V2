#include "opencv2/opencv.hpp"

using namespace std;

class MouseEvent
{
public:
	MouseEvent() {}

	void HandleEvent(int evt, int x, int y, int flags);

	vector<cv::Point2i> position;

	int state = 0;
};

void MouseEvent::HandleEvent(int evt, int x, int y, int flags)
{
	switch (evt)
	{
	case CV_EVENT_LBUTTONDOWN:
		position.clear();
		position.push_back(cv::Point2i(x, y));
		state = 1;
		break; 

	case CV_EVENT_LBUTTONUP:
		position.push_back(cv::Point2i(x, y));
		state = 0;
		break;

	case CV_EVENT_MOUSEMOVE:
		switch (state)
		{
		case 1:
			position.push_back(cv::Point2i(x, y));
			state = 2;
			break;

		case 2:
			position.push_back(cv::Point2i(x, y));
			break;
		}
		break;
	}
}