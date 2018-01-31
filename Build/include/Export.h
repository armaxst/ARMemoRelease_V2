#pragma once

#if defined ARDRAWING_API_EXPORTS
#define ARDRAWING_API __declspec(dllexport)
#else
#define ARDRAWING_API
#endif

#if defined ARDRAWING_API_EXPORTS
#define MAXSTAR_API __declspec(dllexport)
#else
#define MAXSTAR_API
#endif
