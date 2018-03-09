#include <iostream>

#include "opencv2/opencv.hpp"
#include "ARMemo.h"

#include "MouseEvent.h"
#include "Define.h"

#define CAMERA_WIDTH 640
#define CAMERA_HEIGHT 480
#define CAMERA_INDEX 0

#define USE_CAMERA 1
#define VIDEO_PATH "null.mp4"

using namespace std;

void help() {
	cout << "This program is ARMemo sample Application.\n" << endl;
	cout <<
		"Features \n" <<
		"Q : capture image and start drawing \n" <<
		"W : start learning with input image and stroke \n" <<
		"E : save data \n" <<
		"A : load data and start tracking \n" <<
		"S : stop tracking \n" <<
		"ESC : exit this program" << endl;
}

enum ColorFormat
{
	GRAY = 0,
	RGB,
	YUV420sp
};

void onMouse(int evt, int x, int y, int flags, void* param) {
	MouseEvent* p = (MouseEvent*)param;
	p->HandleEvent(evt, x, y, flags);
}

void keyboardEvent(int inputKey, State* state) {
	switch (inputKey) {
		case 'q':
		case 'Q':
			*state = CAPTURE_IMAGE;
			break;
		case 'w':
		case 'W':
			*state = LEARNING;
			break;
		case 'e':
		case 'E':
			*state = SAVE;
			break;
		case 'a':
		case 'A':
			*state = START_TRACKING;
			break;
		case 's':
		case 'S':
			*state = STOP_TRACKING;
			break;
		case 'f':
		case 'F':
			*state = CHANGE_RESOLUTION;
			break;
		case 27: //esc
			*state = EXIT;
			break;
		default:
			break;
	}
}

void getMiddlePoint(vector<cv::Point2i> stroke, cv::Point2f* middle) {
	int minX = 9999;
	int minY = 9999;
	int maxX = -1;
	int maxY = -1;

	for (int i = 0; i < stroke.size(); i++) {
		if (stroke[i].x < minX) {
			minX = stroke[i].x;
		}

		if (stroke[i].x > maxX) {
			maxX = stroke[i].x;
		}

		if (stroke[i].y < minY) {
			minY = stroke[i].y;
		}

		if (stroke[i].y > maxY) {
			maxY = stroke[i].y;
		}
	}
	middle->x = (minX + maxX) / 2;
	middle->y = (minY + maxY) / 2;
}

void main() {
	MouseEvent* mEvent = new MouseEvent;

	string windowName = "ARMemo windows sample application";

	cv::namedWindow(windowName);
	cv::moveWindow(windowName, 500, 200);
	cv::setMouseCallback(windowName, onMouse, mEvent);

	help();

	int apiResultCode = armemo::initialize();
	if (apiResultCode != 0) {
		cout << "initialize fail, error code : " << apiResultCode << endl;
		return;
	}
	cout << "initialize success" << endl;

#if USE_CAMERA
	cv::VideoCapture capture(CAMERA_INDEX);
	if (!capture.isOpened()) {
		cout << "camera open fail, file or camera Idx : " << CAMERA_INDEX << endl;
		return;
	}
	capture.set(CV_CAP_PROP_FRAME_HEIGHT, CAMERA_HEIGHT);
	capture.set(CV_CAP_PROP_FRAME_WIDTH, CAMERA_WIDTH);
	cout << "caemra open success" << endl;
#else
	cv::VideoCapture capture(VIDEO_PATH);
	if (!capture.isOpened()) {
		cout << "camera open fail, file or camera Idx : " << VIDEO_PATH << endl;
		return;
	}
	double fps = capture.get(CV_CAP_PROP_FPS);
#endif

	apiResultCode = armemo::start();
	if (apiResultCode != 0) {
		cout << "start fail, error code : " << apiResultCode << endl;
		return;
	}
	cout << "start success" << endl;

	cv::Mat image;
	cv::Mat captureImage;

	bool isLoop = true;
	State state = IDLE;
	vector<cv::Point2i> learnedStroke;

	while (isLoop) {
		if (state != DRAWING) {
#if USE_CAMERA
			capture >> image;
#else
			if (capture.get(CV_CAP_PROP_POS_FRAMES) <= (capture.get(CV_CAP_PROP_FRAME_COUNT) - 15)) {
				capture >> image;
				//cv::resize(image, image, cv::Size(1280, 720));
				//image = cv::imread("errorCase/1497321180298.jpg");
			}
			else {
				capture.set(CV_CAP_PROP_POS_MSEC, 0);
			}
#endif
		}

#if USE_CAMERA
		int inputKey = cv::waitKey(1);
#else
		int inputKey = cv::waitKey(1000 / (fps + 4));
#endif
		keyboardEvent(inputKey, &state);

		switch (state) {
			case IDLE:
				cv::imshow(windowName, image);
				break;

			case CAPTURE_IMAGE:
				armemo::clearLearnedTrackable();

				if (!captureImage.empty()) {
					captureImage.release();
				}
			case CAPTURE_IMAGE + 1:
			case CAPTURE_IMAGE + 2:
			case CAPTURE_IMAGE + 3:
			case CAPTURE_IMAGE + 4:
			case CAPTURE_IMAGE + 5:
			case CAPTURE_IMAGE + 6:
			case CAPTURE_IMAGE + 7:
			case CAPTURE_IMAGE + 8:
			case CAPTURE_IMAGE + 9:
			case CAPTURE_IMAGE + 10:
			case CAPTURE_IMAGE + 11:
			case CAPTURE_IMAGE + 12:
			case CAPTURE_IMAGE + 13:
			case CAPTURE_IMAGE + 14:
			case CAPTURE_IMAGE + 15:
				apiResultCode = armemo::checkLearnable(
														image.data,
														image.cols * image.rows * 3,
														image.cols, 
														image.rows,
														RGB);
				if (apiResultCode == 0) {
					cout << "checkLearnable success" << endl;
					state = DRAWING;
					captureImage = image.clone();
					mEvent->position.clear();
				}
				else {
					state = (State)(state + 1);

					if (state > 20) {
						cout << "checkLearnable fail, error code : " << apiResultCode << endl;

						state = IDLE;
					}
				}
				break;

			case DRAWING:
				if (!captureImage.empty()) {
					cv::Mat pauseImage = captureImage.clone();
					for (int i = 0; i < (int)mEvent->position.size() - 1; i++) {
						cv::line(pauseImage, mEvent->position[i], mEvent->position[i + 1], cv::Scalar(0, 255, 0), 2);
					}
					cv::imshow(windowName, pauseImage);
				}
				else {
					cout << "do 'checkLearnable' first!!" << endl;
					state = IDLE;
				}
				break;

			case LEARNING:
				if (!captureImage.empty() && !mEvent->position.empty()) {
					learnedStroke.clear();
					learnedStroke.assign(mEvent->position.begin(), mEvent->position.end());
					mEvent->position.clear();

					int* stroke = new int[(int)learnedStroke.size() * 2];

					for (int i = 0; i < (int)learnedStroke.size(); i++) {
						stroke[2 * i + 0] = learnedStroke[i].x;
						stroke[2 * i + 1] = learnedStroke[i].y;
					}

					int learningTime = 0;

					apiResultCode = armemo::learn(image.data,
												  image.cols * image.rows * 3,
												  image.cols, image.rows, 
												  RGB, 
												  stroke, 
												  (int)learnedStroke.size());

					if (apiResultCode == 0) {
						cout << "learn Success" << endl;
					}
					else {
						cout << "learn fail, error code : " << apiResultCode << endl;
					}

					delete[] stroke;
					captureImage.release();
					state = IDLE;
				}
				else {
					if (captureImage.empty()) {
						cout << "do 'checkLearnable' first!!" << endl;
					}
					if (mEvent->position.empty()) {
						cout << "draw please" << endl;
					}
					state = IDLE;
				}
				break;

			case SAVE:
				apiResultCode = armemo::saveLearnedFile("uplus.armemo");

				if (apiResultCode == 0) {
					cout << "save Success" << endl;

					apiResultCode = armemo::clearLearnedTrackable();
					if (apiResultCode == 0) {
						cout << "clear learned trackable Success" << endl;
					}
					else {
						cout << "clear learned trackable fail" << endl;
					}

					state = IDLE;
				}
				else {
					cout << "save fail, please retry save" << endl;
					state = IDLE;
				}
				break;

			case START_TRACKING:
				state = TRACKING;
				armemo::start();
				apiResultCode = armemo::setTrackingFile("uplus.armemo");

				if (apiResultCode == 0) {
					cout << "file load Success" << endl;
				}
				else { 
					cout << "file load fail" << endl;
				}
			break;

			case TRACKING:
			{
				apiResultCode = armemo::inputTrackingImage(
															image.data, 
															image.cols * image.rows * 3,
															image.cols,
															image.rows,
															RGB);
				if (apiResultCode == 0) {
				}
				else {
					cout << "inputTrackingImage fail, error code : " << apiResultCode << endl;
				}

				float transformMatrix[9];
				int trackingTime = 0;
				int outputImageIdx = 0;

				apiResultCode = armemo::getTrackingResult(transformMatrix);
				
				if (apiResultCode == 0) {
					cv::Mat transformMatrix3x3(3, 3, CV_32FC1, transformMatrix);
					
					cv::Mat point1(3, 1, CV_32F);
					cv::Mat point2(3, 1, CV_32F);

					float* point1Pointer = (float*)point1.data;
					float* point2Pointer = (float*)point2.data;

					for (int i = 0; i < (int)learnedStroke.size() - 1; i++) {
						point1Pointer[0] = (float)learnedStroke[i].x;
						point1Pointer[1] = (float)learnedStroke[i].y;
						point1Pointer[2] = (float)1.f;

						point2Pointer[0] = (float)learnedStroke[i + 1].x;
						point2Pointer[1] = (float)learnedStroke[i + 1].y;
						point2Pointer[2] = (float)1.f;

						cv::Mat result1 = transformMatrix3x3 * point1;
						cv::Mat result2 = transformMatrix3x3 * point2;

						float* result1Pointer = (float*)result1.data;
						float* result2Pointer = (float*)result2.data;

						cv::line(image,
								 cv::Point2i((int)(result2Pointer[0] / result2Pointer[2]), (int)(result2Pointer[1] / result2Pointer[2])),
								 cv::Point2i((int)(result1Pointer[0] / result1Pointer[2]), (int)(result1Pointer[1] / result1Pointer[2])),
								 cv::Scalar(0, 255, 0), 2);
					}

					for (int i = 0; i < (int)mEvent->position.size() - 1; i++) {
						cv::line(image, mEvent->position[i], mEvent->position[i + 1], cv::Scalar(0, 255, 0), 2);
					}
				}
				else {
					cout << "getTrackingResult fail, error code : " << apiResultCode << endl;
				}

				cv::imshow(windowName, image);
			}
			break;

			case STOP_TRACKING:
				armemo::clearLearnedTrackable();
				armemo::clearTrackingTrackable();
				apiResultCode = armemo::stop();

				if (apiResultCode == 0) {
				}
				else {
					cout << "stop fail, error code : " << apiResultCode << endl;
				}

				state = IDLE;
				break;

			case CHANGE_RESOLUTION:
				state = IDLE;
				armemo::clearLearnedTrackable();
				armemo::clearTrackingTrackable();
				armemo::stop();
				armemo::destroy();

				if (capture.get(cv::CAP_PROP_FRAME_HEIGHT) == 480) {
					capture.release();
					capture.open(0);
					capture.set(CV_CAP_PROP_FRAME_HEIGHT, 720);
					capture.set(CV_CAP_PROP_FRAME_WIDTH, 1280);
				} 
				else {
					capture.release();
					capture.open(0);
					capture.set(CV_CAP_PROP_FRAME_HEIGHT, 480);
					capture.set(CV_CAP_PROP_FRAME_WIDTH, 640);
				}

				armemo::initialize();
				armemo::start();

				break;
			case EXIT:
				armemo::clearLearnedTrackable();
				armemo::clearTrackingTrackable();
				armemo::stop();
				armemo::destroy();

				isLoop = false;
				break;
			default:
				break;
		}

	}
}
