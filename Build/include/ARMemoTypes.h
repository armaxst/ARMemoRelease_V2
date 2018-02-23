#pragma once

#if defined ARMEMO_API_EXPORTS
#define ARMEMO_API __declspec(dllexport)
#else
#define ARMEMO_API
#endif
