# Frontend Impact Analysis
## API Changes & Integration Requirements

**Document Version:** 1.0  
**Date:** June 19, 2025  
**Backend Changes:** QueryDSL Projection Fixes & Security Updates  
**Target:** Client (Next.js) & Frontend (React+Vite) Applications  

---

## 📋 Overview

This document outlines all frontend changes required due to backend modifications, particularly focusing on API contract changes, new security requirements, and updated response formats.

---

## 🔄 API Contract Changes

### 1. **Quiz Summary Response Updates**
**Impact Level:** 🟡 **Medium** - Response format changes

**Changes Made:**
- `QuizSummaryResponse` DTO structure updated
- Removed some fields from direct database projection
- Tags are now loaded separately for better performance

**Before:**
```typescript
interface QuizSummaryResponse {
  id: number;
  title: string;
  description: string; // REMOVED from some endpoints
  quizType: string;
  difficultyLevel: string;
  questionCount: number;
  creatorUsername: string; // REMOVED from some endpoints
  creatorProfileImage: string; // REMOVED from some endpoints
  viewCount: number; // REMOVED from some endpoints
  attemptCount: number;
  avgScore: number;
  tags: TagResponse[];
  createdAt: string;
}
```

**After:**
```typescript
interface QuizSummaryResponse {
  id: number;
  title: string;
  quizType: string;
  difficultyLevel: string;
  questionCount: number;
  attemptCount: number;
  avgScore: number;
  tags: TagResponse[]; // Always populated
  createdAt: string;
}
```

**Frontend Action Required:**
- ✅ Update TypeScript interfaces
- ✅ Remove references to deleted fields in UI components
- ✅ Test quiz listing and search functionality

---

### 2. **Quiz Detail Response Updates**
**Impact Level:** 🟢 **Low** - Additive changes only

**Changes Made:**
- Added new fields to `QuizDetailResponse` for better data access
- Maintains backward compatibility

**New Fields Added:**
```typescript
interface QuizDetailResponse {
  // ... existing fields ...
  creatorId: number;           // NEW
  creatorUsername: string;     // NEW  
  creatorProfileImage: string; // NEW
  viewCount: number;           // NEW
  isPublic: boolean;           // NEW
  // ... rest unchanged ...
}
```

**Frontend Action Required:**
- ✅ Update TypeScript interfaces to include new optional fields
- ✅ Consider using new fields for enhanced UI features (creator info, public status)
- ✅ Test quiz detail pages

---

### 3. **Quiz Type Enum Updates**
**Impact Level:** 🟢 **Low** - Additive change

**Changes Made:**
- Added `TAG_BASED` to `QuizType` enum

**Updated Enum:**
```typescript
enum QuizType {
  REGULAR = "REGULAR",
  DAILY = "DAILY", 
  WEEKLY = "WEEKLY",
  SPECIAL = "SPECIAL",
  BATTLE = "BATTLE",
  TAG_BASED = "TAG_BASED" // NEW
}
```

**Frontend Action Required:**
- ✅ Update TypeScript enum definitions
- ✅ Add UI support for tag-based quiz type if needed
- ✅ Update quiz type filtering components

---

### 4. **Error Response Standardization**
**Impact Level:** 🟠 **High** - Breaking change for error handling

**Changes Made:**
- Backend will standardize all error responses to use `CommonApiResponse` format
- Eliminates inconsistent error response formats

**New Standard Error Format:**
```typescript
interface CommonApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string;
  errorCode?: string;
  timestamp: string;
}

// All errors will now return:
interface ErrorResponse extends CommonApiResponse<null> {
  success: false;
  data: null;
  message: string;
  errorCode: string;
  timestamp: string;
}
```

**Frontend Action Required:**
- ✅ **CRITICAL:** Update all error handling code to expect consistent format
- ✅ Modify API client error interceptors
- ✅ Update toast/notification systems
- ✅ Test all error scenarios (validation, auth, server errors)

---

## 🔐 Security Updates

### 5. **Authentication Flow Changes**
**Impact Level:** 🟠 **High** - Enhanced security requirements

**Changes Made:**
- Stricter JWT validation
- Enhanced user profile access controls
- Admin endpoint security hardening

**Frontend Changes Required:**

**A. Enhanced JWT Handling:**
```typescript
// Update JWT token management
export const authApi = {
  // Add more robust token validation
  validateToken: async (token: string) => {
    // Enhanced validation logic
    if (!token || token.split('.').length !== 3) {
      throw new Error('Invalid token format');
    }
    // ... existing validation
  },
  
  // Improved token refresh logic
  refreshToken: async () => {
    // Handle new error scenarios
    try {
      const response = await httpClient.post('/auth/refresh');
      return response.data;
    } catch (error) {
      // New standardized error format
      if (error.response?.data?.errorCode === 'TOKEN_EXPIRED') {
        // Handle gracefully
        logout();
      }
      throw error;
    }
  }
};
```

**B. User Profile Access Updates:**
```typescript
// Update user profile access patterns
export const useUserProfile = (userId: string) => {
  const { data: currentUser } = useAuth();
  
  // Add client-side access check
  const canViewProfile = currentUser?.id === userId || currentUser?.role === 'ADMIN';
  
  return useQuery({
    queryKey: ['userProfile', userId],
    queryFn: () => userApi.getUserProfile(userId),
    enabled: canViewProfile, // Only fetch if authorized
    retry: (failureCount, error) => {
      // Don't retry on 403 Forbidden
      if (error.response?.status === 403) return false;
      return failureCount < 3;
    }
  });
};
```

**Frontend Action Required:**
- ✅ **CRITICAL:** Update authentication state management
- ✅ **CRITICAL:** Implement proper error handling for 403 Forbidden responses  
- ✅ Add client-side authorization checks
- ✅ Update profile access components
- ✅ Test authentication flows thoroughly

---

### 6. **CORS Configuration Updates**
**Impact Level:** 🟢 **Low** - Environment-specific

**Changes Made:**
- More restrictive CORS settings in production
- Explicit allowed origins instead of wildcard

**Frontend Action Required:**
- ✅ Ensure frontend domains are in backend CORS whitelist
- ✅ Update development vs production API base URLs
- ✅ Test cross-origin requests in staging environment

---

## 🚀 New Features & Endpoints

### 7. **Enhanced Daily Quiz Support**
**Impact Level:** 🟢 **Low** - Optional enhancement

**Changes Made:**
- Improved daily quiz error handling
- Better "no daily quiz available" responses

**New Error Handling:**
```typescript
// Update daily quiz fetching
export const useDailyQuiz = () => {
  return useQuery({
    queryKey: ['dailyQuiz'],
    queryFn: async () => {
      try {
        const response = await quizApi.getDailyQuiz();
        return response.data;
      } catch (error) {
        if (error.response?.data?.message === '오늘의 데일리 퀴즈가 없습니다') {
          // Handle gracefully - show "no quiz today" message
          return null;
        }
        throw error;
      }
    },
    staleTime: 60 * 60 * 1000, // Cache for 1 hour
  });
};
```

**Frontend Action Required:**
- ✅ Update daily quiz components to handle null/empty responses
- ✅ Add better "no daily quiz" user experience
- ✅ Test daily quiz functionality

---

## 📱 UI/UX Considerations

### 8. **Quiz Display Updates**
**Impact Level:** 🟡 **Medium** - UI adjustments needed

**Changes Due To:**
- Removal of some fields from quiz summary responses
- New fields available in detail responses

**UI Updates Required:**

**A. Quiz Cards/Lists:**
```tsx
// Remove fields no longer available in summary
const QuizCard = ({ quiz }: { quiz: QuizSummaryResponse }) => {
  return (
    <div className="quiz-card">
      <h3>{quiz.title}</h3>
      {/* REMOVE: description, creatorUsername, viewCount not in summary */}
      {/* <p>{quiz.description}</p> */}
      {/* <span>by {quiz.creatorUsername}</span> */}
      {/* <span>{quiz.viewCount} views</span> */}
      
      {/* KEEP: these are still available */}
      <span>{quiz.quizType}</span>
      <span>{quiz.difficultyLevel}</span>
      <span>{quiz.attemptCount} attempts</span>
      <span>Score: {quiz.avgScore}</span>
    </div>
  );
};
```

**B. Quiz Detail Pages:**
```tsx
// Use new detailed fields when available
const QuizDetail = ({ quiz }: { quiz: QuizDetailResponse }) => {
  return (
    <div className="quiz-detail">
      <h1>{quiz.title}</h1>
      <p>{quiz.description}</p>
      
      {/* Use new creator fields */}
      <div className="creator-info">
        <img src={quiz.creatorProfileImage} alt="Creator" />
        <span>{quiz.creatorUsername}</span>
      </div>
      
      {/* Use new view count and public status */}
      <div className="stats">
        <span>{quiz.viewCount} views</span>
        {quiz.isPublic && <span className="public-badge">Public</span>}
      </div>
    </div>
  );
};
```

**Frontend Action Required:**
- ✅ Update all quiz display components
- ✅ Remove references to unavailable fields in summary views
- ✅ Utilize new fields in detail views for enhanced UX
- ✅ Update CSS/styling as needed

---

## 🔧 Development & Testing Requirements

### 9. **API Client Updates**
**Impact Level:** 🟠 **High** - Code changes required

**Required Updates:**

**A. Type Definitions:**
```typescript
// Create new type definitions file: types/api.ts
export interface QuizSummaryResponse {
  id: number;
  title: string;
  quizType: QuizType;
  difficultyLevel: DifficultyLevel;
  questionCount: number;
  attemptCount: number;
  avgScore: number;
  tags: TagResponse[];
  createdAt: string;
}

export interface QuizDetailResponse extends QuizSummaryResponse {
  description: string;
  timeLimit: number;
  creatorId: number;
  creatorUsername: string;
  creatorProfileImage: string;
  viewCount: number;
  isPublic: boolean;
  creator: UserSummaryResponse;
  statistics: QuizStatistics;
}

export enum QuizType {
  REGULAR = "REGULAR",
  DAILY = "DAILY",
  WEEKLY = "WEEKLY", 
  SPECIAL = "SPECIAL",
  BATTLE = "BATTLE",
  TAG_BASED = "TAG_BASED"
}
```

**B. Error Handling:**
```typescript
// Update API client: lib/api/httpClient.ts
export const httpClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle new standardized error format
    if (error.response?.data) {
      const errorData = error.response.data;
      if (errorData.success === false) {
        // New standardized format
        throw new ApiError(errorData.message, errorData.errorCode, error.response.status);
      }
    }
    throw error;
  }
);
```

**Frontend Action Required:**
- ✅ **CRITICAL:** Update all TypeScript interfaces
- ✅ **CRITICAL:** Update API client error handling
- ✅ Update all API hook functions (React Query)
- ✅ Run type checking across entire codebase

---

### 10. **Testing Requirements**
**Impact Level:** 🟠 **High** - Comprehensive testing needed

**Test Categories:**

**A. API Integration Tests:**
- ✅ Verify all quiz-related API calls work with new response formats
- ✅ Test error handling with new standardized error responses
- ✅ Validate authentication flows and 403 handling
- ✅ Test daily quiz edge cases (no quiz available)

**B. Component Tests:**
- ✅ Update component tests for new prop types
- ✅ Test quiz cards with reduced field set
- ✅ Test quiz detail pages with new fields
- ✅ Test error boundary components with new error formats

**C. E2E Tests:**
- ✅ Test complete user journeys (signup, login, quiz taking)
- ✅ Test error scenarios (network failures, auth errors)
- ✅ Test responsive design with updated components

---

## 📋 Migration Checklist

### Pre-Deployment Tasks
- [ ] **Update Type Definitions** - All interfaces updated
- [ ] **Update API Client** - Error handling and request formats  
- [ ] **Update Components** - Remove unavailable fields, add new fields
- [ ] **Update Error Handling** - Standardized error response format
- [ ] **Update Authentication** - Enhanced security and validation
- [ ] **Test All Flows** - API calls, UI components, error scenarios

### Deployment Coordination
- [ ] **Backend Deployment** - Ensure backend changes are live first
- [ ] **Frontend Deployment** - Deploy updated frontend code
- [ ] **Smoke Testing** - Verify critical paths work
- [ ] **Rollback Plan** - Prepare rollback procedure if issues arise

### Post-Deployment Validation
- [ ] **API Response Validation** - Verify new formats in production
- [ ] **User Authentication** - Test login/logout flows
- [ ] **Quiz Functionality** - Test quiz viewing, taking, results
- [ ] **Error Scenarios** - Verify error handling works correctly
- [ ] **Performance** - Monitor for any performance regressions

---

## 🚨 Breaking Changes Summary

### **High Priority (Must Fix Before Deploy)**
1. **Error Response Format** - All error handling code needs updates
2. **Quiz Summary Fields** - UI components must remove unavailable fields
3. **Authentication Security** - Enhanced 403 error handling required

### **Medium Priority (Should Fix)**
1. **Quiz Type Enum** - Add support for new TAG_BASED type
2. **Quiz Detail Fields** - Utilize new fields for better UX

### **Low Priority (Nice to Have)**
1. **Daily Quiz Handling** - Better empty state handling
2. **Performance** - Utilize new optimized data loading patterns

---

## 📞 Support & Questions

**For Technical Questions:**
- Review backend code changes in repository
- Check API documentation updates
- Test against development/staging environment

**For UI/UX Questions:**
- Review component impact analysis above
- Test UI changes in development environment
- Validate responsive design still works

**For Security Questions:**
- Review authentication flow changes
- Verify CORS settings for your domain
- Test all authenticated API calls

---

**Document Status:** ✅ **Complete** - Ready for frontend team review and implementation