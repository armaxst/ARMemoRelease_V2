#define _CRT_SECURE_NO_WARNINGS

#include <iostream>
#include <fstream>

#include <opencv2/opencv.hpp>

#include "ARDrawingAPI.h"
#include "ARDrawingTest.h"

#include "MouseEvent.h"
#include "Define.h"
#include <crtdbg.h>

#define CAMERA_WIDTH 1280
#define CAMERA_HEIGHT 720
#define CAMERA_INDEX 0

#define USE_CAMERA 1
#define VIDEO_PATH "20170816_133235.mp4"

using namespace std;

int apiResultCode = 0;
bool testMode = false;

void help() {
	cout << "This program is ARMemo sample Application.\n" << endl;

	cout << "Mode Change(Normal <-> Test) \n" <<
		"1 : Normal \n" <<
		"2 : Test \n" << endl;

	cout << "(Test mode don't use multi thread.)" << endl;

	cout <<
		"Features \n" <<
		"Q : capture image and start drawing \n" <<
		"W : start learning with input image and stroke \n" <<
		"E : start tracking \n" <<
		"R : stop tracking \n" <<
		"ESC : exit this program" << endl;

}

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
		*state = START_TRACKING;
		break;
	case 'r':
	case 'R':
		*state = STOP_TRACKING;
		break;
	case 'a':
	case 'A':
		*state = ADD_DRAWING;
		break;
	case '1':
		if (*state == TRACKING) {
			startTracking();
		}
		cout << "*************************Normal mode.************************" << endl;
		testMode = false;
		break;
	case '2':
		if (*state == TRACKING) {
			stopTracking();
		}
		cout << "*************************Test mode.**************************" << endl;
		testMode = true;
		break;
	case 27: //esc
		*state = EXIT;
		break;
	default:
		break;
	}
}

int fileNum = 0;

cv::Mat readImage(string filePath) {
	string fileName = filePath;
	string videoName = to_string(fileNum);
	if (fileNum < 1000 && fileNum >= 100) {
		videoName = "0" + videoName;
	}
	else if (fileNum < 100 && fileNum >= 10) {
		videoName = "00" + videoName;
	}
	else if (fileNum < 10) {
		videoName = "000" + videoName;
	}
	fileNum++;

	fileName = fileName + "/frame_" + videoName + ".dat";
	/****   C  *******************/
	FILE* fp;
	fopen_s(&fp, fileName.c_str(), "rb");

	if (fp == NULL) {
		//err
		cout << "file open error " << fileNum - 1 << endl;
		return cv::Mat::zeros(3, 3, CV_8UC1);
	}
	int* test = new int[1];
	int width = 1280;
	int height = 720;

	/*char* imageSIze = new char[4];
	fread(imageSIze, sizeof(char), 4, fp);
	width = (imageSIze[0] & 0xff) << 24 | (imageSIze[1] & 0xff) << 16 |
	(imageSIze[2] & 0xff) << 8 | (imageSIze[3] & 0xff);
	fread(imageSIze, sizeof(char), 4, fp);
	height = (imageSIze[0] & 0xff) << 24 | (imageSIze[1] & 0xff) << 16 |
	(imageSIze[2] & 0xff) << 8 | (imageSIze[3] & 0xff);*/
	char* dataArray2 = new char[width * height * 4];
	fread(dataArray2, sizeof(char), width * height * 4, fp);
	cv::Mat yuvArray2 = cv::Mat(height, width, CV_8UC4, dataArray2);

	fclose(fp);
	/****   C end *******************/

	return yuvArray2;
}

cv::Mat readImage_iWillData(string filePath) {
	string fileName = filePath;
	fileName = fileName;

	/****   C  *******************/
	FILE* fp;
	fopen_s(&fp, fileName.c_str(), "rb");

	if (fp == NULL) {
		//err
		cout << "file open error " << fileNum - 1 << endl;
		return cv::Mat::zeros(3, 3, CV_8UC1);
	}
	int* test = new int[1];
	int width = 1280;
	int height = 720;

	/*char* imageSIze = new char[4];
	fread(imageSIze, sizeof(char), 4, fp);
	width = (imageSIze[0] & 0xff) << 24 | (imageSIze[1] & 0xff) << 16 |
	(imageSIze[2] & 0xff) << 8 | (imageSIze[3] & 0xff);
	fread(imageSIze, sizeof(char), 4, fp);
	height = (imageSIze[0] & 0xff) << 24 | (imageSIze[1] & 0xff) << 16 |
	(imageSIze[2] & 0xff) << 8 | (imageSIze[3] & 0xff);*/
	char* dataArray2 = new char[width * height * 4];
	fread(dataArray2, sizeof(char), width * height * 4, fp);
	cv::Mat yuvArray2 = cv::Mat(height, width, CV_8UC3, dataArray2);

	fclose(fp);
	/****   C end *******************/

	return yuvArray2;
}

vector<cv::KeyPoint> fastCorners;

void main() {
	_CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF);
	//_CrtSetBreakAlloc(588);
	MouseEvent* mEvent = new MouseEvent;

	char version[13];
	memcpy(version, getEngineVersion(), sizeof(char) * 12);
	version[12] = '\0';
	string windowName = version;

	cv::namedWindow(windowName);
	cv::moveWindow(windowName, 100, 50);
	cv::setMouseCallback(windowName, onMouse, mEvent);

	apiResultCode = initialize();
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

	help();

	cv::Mat image;
	cv::Mat captureImage;

	bool isLoop = true;

	State state = IDLE;

	vector<unsigned char> trackable;
	vector<cv::Point2i> learnedStroke;

	int imageIdx = 0;

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
		imageIdx++;
		setRecognitionDelay(150);

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
			apiResultCode = checkLearnable(image.data, image.cols, image.rows, 1, 70.0f);
			if (apiResultCode == 0) {
				cout << "checkLearnable success" << endl;
				state = DRAWING;
				captureImage = image.clone();
				mEvent->position.clear();
			}
			else {
				cout << "checkLearnable fail, error code : " << apiResultCode << endl;
				state = IDLE;

				if (!captureImage.empty()) {
					captureImage.release();
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

				//apiResultCode = learn(captureImage.data, captureImage.cols, captureImage.rows, 1, stroke, 4, &learningTime);
				apiResultCode = learn(captureImage.data, captureImage.cols, captureImage.rows, 1, stroke, (int)learnedStroke.size(), &learningTime);

				cv::Mat grayTemp;
				cv::cvtColor(captureImage, grayTemp, CV_RGB2GRAY);
				cv::FAST(grayTemp, fastCorners, 50);

				if (apiResultCode == 0) {
					cout << "learn Success, learning time : " << learningTime << " ms" << endl;
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
		case START_TRACKING:
		{
			capture.set(CV_CAP_PROP_FRAME_HEIGHT, CAMERA_HEIGHT);
			capture.set(CV_CAP_PROP_FRAME_WIDTH, CAMERA_WIDTH);

			int trackableByteSize = 0;

			if (testMode == false) {
				apiResultCode = startTracking();

				if (apiResultCode == 0) {
					state = TRACKING;
				}
				else {
					cout << "startTracking fail, error code : " << apiResultCode << endl;
					state = TRACKING;
				}
			}
			else {
				state = TRACKING;
			}

			apiResultCode = getLearnedTrackableArraySize(&trackableByteSize);

			if (apiResultCode == 0) {
				unsigned char* trackableByteArray = new unsigned char[trackableByteSize];

				apiResultCode = getLearnedTrackableArray(trackableByteArray);

				/*FILE* file;
				file = fopen("testTrackable/android_learn result data.trk", "rb");
				delete[] trackableByteArray;
				trackableByteArray = new unsigned char[451540];
				trackableByteSize = 451540;
				fread(trackableByteArray, sizeof(unsigned char), trackableByteSize, file);*/

				/*delete[] trackableByteArray;

				trackableByteSize = 321168;
				trackableByteArray = new unsigned char[trackableByteSize];

				FILE* file;
				fopen_s(&file, "AR메모_이격시험_결과데이터_맥스트/시험_케이스1(6장)/2.다른단말_동일데이터_테스트/학습데이터.trk", "rb");
				fread(trackableByteArray, sizeof(char), trackableByteSize, file);*/

				if (apiResultCode == 0) {
				}
				else {
					cout << "getLearnedTrackableArray fail, error code : " << apiResultCode << endl;
				}

				if (testMode == false) {
					apiResultCode = clearTrackingTrackable();
					apiResultCode = clearLearnedTrackable();

					if (apiResultCode == 0) {
					}
					else {
						cout << "clearLearnedTrackable fail, error code : " << apiResultCode << endl;
					}
				}
				else {
					ARDrawing::ARDrawingTest::getInstance()->unloadTrackerData();
				}

				if (testMode == false) {
					apiResultCode = setTrackingTrackableArray(trackableByteArray, trackableByteSize);

					if (apiResultCode == 0) {
					}
					else {
						cout << "setTrackingTrackable fail, error code : " << apiResultCode << endl;
					}
				}
				else {
					ARDrawing::ARDrawingTest::getInstance()->loadTrackerData(trackableByteArray, trackableByteSize);
				}

				delete[] trackableByteArray;
			}
			else {
				cout << "getLearnedTrackableArraySize fail, error code : " << apiResultCode << endl;
			}
		}
		break;
		case TRACKING:
		{
			apiResultCode = inputTrackingImage(image.data, image.cols, image.rows, 1, imageIdx);
			cv::Mat grayTemp;
			cv::cvtColor(image, grayTemp, CV_RGB2GRAY);
			if (apiResultCode == 0) {
			}
			else {
				cout << "inputTrackingImage fail, error code : " << apiResultCode << endl;
			}

			if (testMode == true) {
				ARDrawing::ARDrawingTest::getInstance()->trackImage();
			}

			float* transformMatrix = new float[9];
			int trackingTime = 0;
			int outputImageIdx = 0;

			cv::Mat temp(720, 1280, CV_8UC1);
			getTrackingResultWithImage(transformMatrix, &trackingTime, &outputImageIdx, temp.data);
			cv::imshow("temp", temp);

			apiResultCode = getTrackingResult(transformMatrix, &trackingTime, &outputImageIdx);
			//cv::waitKey(200);
			if (apiResultCode == 0) {
				cout << "trackingTime : " << trackingTime << " ms, image index : " << outputImageIdx << endl;
				cv::Mat transformMatrix3x3(3, 3, CV_32FC1, transformMatrix);
				cout << transformMatrix3x3 << endl;
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


				/*for (int i = 0; i < (int)fastCorners.size(); i++) {
					point1Pointer[0] = (float)fastCorners[i].pt.x;
					point1Pointer[1] = (float)fastCorners[i].pt.y;
					point1Pointer[2] = (float)1.f;

					cv::Mat result1 = transformMatrix3x3 * point1;

					float* result1Pointer = (float*)result1.data;

					cv::circle(image, cv::Point2i((int)(result1Pointer[0] / result1Pointer[2]), (int)(result1Pointer[1] / result1Pointer[2])), 2, cv::Scalar(0, 0, 255), -1);
				}

				vector<cv::KeyPoint> tempFastCorners;
				
				cv::FAST(grayTemp, tempFastCorners, 50);

				for (int i = 0; i < (int)tempFastCorners.size(); i++) {
					cv::circle(image, tempFastCorners[i].pt, 2, cv::Scalar(255, 0, 0), -1);
				}*/
			}
			else {
				cout << "getTrackingResult fail, error code : " << apiResultCode << endl;
			}

			cv::imshow(windowName, image);

			//cv::waitKey();

			delete[] transformMatrix;
		}
		break;
		case STOP_TRACKING:
			if (testMode == false) {
				apiResultCode = stopTracking();
			}

			if (apiResultCode == 0) {
			}
			else {
				cout << "stopTracking fail, error code : " << apiResultCode << endl;
			}

			state = IDLE;

			break;

		case ADD_DRAWING:
		{
			apiResultCode = inputTrackingImage(image.data, image.cols, image.rows, 1, imageIdx);

			if (apiResultCode == 0) {
			}
			else {
				cout << "inputTrackingImage fail, error code : " << apiResultCode << endl;
			}

			if (testMode == true) {
				ARDrawing::ARDrawingTest::getInstance()->trackImage();
			}

			float* transformMatrix = new float[9];
			int trackingTime = 0;
			int outputImageIdx = 0;

			apiResultCode = getTrackingResult(transformMatrix, &trackingTime, &outputImageIdx);

			if (apiResultCode == 0) {
				cout << "trackingTime : " << trackingTime << " ms, image index : " << outputImageIdx << endl;

				cv::Mat transformMatrix3x3(3, 3, CV_32FC1, transformMatrix);

				cv::Mat point1(3, 1, CV_32F);
				cv::Mat point2(3, 1, CV_32F);

				float* point1Pointer = (float*)point1.data;
				float* point2Pointer = (float*)point2.data;

				cv::Mat invT;
				cv::invert(transformMatrix3x3, invT);

				for (int i = 0; i < (int)mEvent->position.size(); i++){
					point1Pointer[0] = (float)mEvent->position[i].x;
					point1Pointer[1] = (float)mEvent->position[i].y;
					point1Pointer[2] = (float)1.f;

					cv::Mat result1 = invT * point1;
					float* result1Pointer = (float*)result1.data;

					learnedStroke.push_back(cv::Point2i((int)(result1Pointer[0] / result1Pointer[2]), (int)(result1Pointer[1] / result1Pointer[2])));
				}
				mEvent->position.clear();

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
			}
			else {
				cout << "getTrackingResult fail, error code : " << apiResultCode << endl;
			}

			state = TRACKING;

			cv::imshow(windowName, image);
			delete[] transformMatrix;
		}
			break;
		case EXIT:
			clearLearnedTrackable();
			if (testMode == false) {
				stopTracking();
				clearTrackingTrackable();
			}
			else {
				ARDrawing::ARDrawingTest::getInstance()->unloadTrackerData();
			}

			isLoop = false;
			break;
		default:
			break;
		}
	}
}