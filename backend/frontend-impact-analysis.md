# Frontend Impact Analysis

## API Contract Changes

### 1. Authentication Endpoints
- **No breaking changes** to existing OAuth2 flows
- **Response format standardization** may affect error parsing
- **Recommendation**: Update error handling to parse new `CommonApiResponse.error()` format

### 2. Quiz APIs

#### Modified Endpoints
- `GET /api/quizzes/search` - Enhanced validation may reject malformed requests
- `GET /api/quizzes/recommended` - New limit validation (1-100)
- `GET /api/quizzes/{id}/play` - Enhanced authorization checks

#### New Response Headers
```http
X-Cache-Status: HIT|MISS|REFRESH
X-Cache: HIT|MISS
```

#### Frontend Updates Needed
```javascript
// Old error handling
if (response.status !== 200) {
  // Handle error
}

// New standardized error handling
if (!response.data.success) {
  console.error(response.data.message, response.data.code);
}
```

### 3. Battle APIs

#### Enhanced Validation
- **Participant limits**: Now validated server-side (2-8 participants)
- **Room status checks**: More strict state validation
- **Authentication**: Enhanced security checks

#### Frontend Updates Needed
```javascript
// Update battle room creation
const createBattleRoom = async (quizId, maxParticipants) => {
  // Add client-side validation
  if (maxParticipants < 2 || maxParticipants > 8) {
    throw new Error('Invalid participant count');
  }
  
  return api.post('/api/battles', {
    quizId,
    maxParticipants
  });
};
```

### 4. User Profile APIs
- **No breaking changes** to existing user endpoints
- **Enhanced error responses** for consistency

## Security Changes Impact

### 1. CORS Configuration
**Current Impact**: More restrictive CORS settings
**Frontend Action Needed**:
```javascript
// Ensure requests include credentials
axios.defaults.withCredentials = true;

// Update allowed origins in development
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'http://ec2-13-125-187-28.ap-northeast-2.compute.amazonaws.com'
  : 'http://localhost:8080';
```

### 2. JWT Token Handling
**Current Impact**: Enhanced token validation
**Frontend Action Needed**:
```javascript
// Update token refresh logic
const refreshToken = async () => {
  try {
    const response = await api.post('/api/oauth2/refresh', {}, {
      headers: {
        'X-Refresh-Token': `Bearer ${getRefreshToken()}`
      }
    });
    
    // Handle new response format
    if (response.data.success) {
      setAccessToken(response.data.data.accessToken);
    }
  } catch (error) {
    // Handle standardized error format
    console.error('Token refresh failed:', error.response.data.message);
  }
};
```

### 3. Request Headers
**New Required Headers**:
```javascript
// Add cache control headers awareness
const api = axios.create({
  headers: {
    'Cache-Control': 'no-cache', // For real-time data
    'Content-Type': 'application/json'
  }
});

// Handle cache status headers
api.interceptors.response.use(response => {
  const cacheStatus = response.headers['x-cache-status'];
  if (cacheStatus) {
    console.debug('Cache status:', cacheStatus);
  }
  return response;
});
```

## New Features Requiring Frontend Support

### 1. Enhanced Search Functionality
**New Search Parameters**:
```typescript
interface QuizSearchRequest {
  title?: string;
  difficultyLevel?: 'EASY' | 'MEDIUM' | 'HARD';
  quizType?: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER';
  tagIds?: number[];
  minQuestions?: number;
  maxQuestions?: number;
  orderBy?: 'createdAt' | 'attemptCount' | 'avgScore' | 'difficultyLevel';
  // New advanced search options
  isPublic?: boolean;
  minAverageScore?: number;
  maxAverageScore?: number;
  minAttempts?: number;
  createdAfter?: string;
  createdBefore?: string;
  creatorId?: number;
}
```

### 2. Battle System Enhancements
**WebSocket Message Changes**:
```typescript
// Enhanced battle progress tracking
interface BattleProgress {
  roomId: number;
  currentQuestionIndex: number;
  participants: ParticipantProgress[];
  timeRemaining: number;
  status: 'WAITING' | 'IN_PROGRESS' | 'COMPLETED';
}

// Updated participant status
interface ParticipantProgress {
  userId: number;
  username: string;
  currentScore: number;
  answeredQuestions: number;
  isReady: boolean;
  isActive: boolean; // New field
}
```

### 3. Cache-Aware Components
**Implement cache-aware data loading**:
```javascript
const useCachedData = (endpoint, options = {}) => {
  const [data, setData] = useState(null);
  const [cacheStatus, setCacheStatus] = useState(null);
  
  useEffect(() => {
    const fetchData = async () => {
      const response = await api.get(endpoint, options);
      setData(response.data);
      setCacheStatus(response.headers['x-cache-status']);
    };
    
    fetchData();
  }, [endpoint]);
  
  return { data, cacheStatus };
};
```

## Error Handling Updates

### 1. Standardized Error Response Format
```typescript
interface APIResponse<T> {
  success: boolean;
  data?: T;
  message: string;
  code?: string;
  timestamp: string;
}

interface ErrorResponse {
  success: false;
  message: string;
  code: string;
  timestamp: string;
  fieldErrors?: FieldError[];
}

interface FieldError {
  field: string;
  value: string;
  reason: string;
}
```

### 2. Updated Error Handling Hook
```javascript
const useApiErrorHandler = () => {
  const showNotification = useNotification();
  
  const handleError = (error) => {
    if (error.response?.data?.success === false) {
      const { message, code, fieldErrors } = error.response.data;
      
      if (fieldErrors) {
        // Handle validation errors
        fieldErrors.forEach(fieldError => {
          showNotification({
            type: 'error',
            message: `${fieldError.field}: ${fieldError.reason}`
          });
        });
      } else {
        // Handle business logic errors
        showNotification({
          type: 'error',
          message,
          code
        });
      }
    } else {
      // Handle network or other errors
      showNotification({
        type: 'error',
        message: 'An unexpected error occurred'
      });
    }
  };
  
  return { handleError };
};
```

## Performance Considerations

### 1. Pagination Updates
**All list endpoints now support pagination**:
```javascript
// Update list components to handle pagination
const QuizList = () => {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  
  const { data, loading } = useApi(`/api/quizzes/search?page=${page}&size=${size}`);
  
  return (
    <div>
      {/* Render quiz list */}
      <Pagination 
        current={page}
        total={data?.totalElements}
        pageSize={size}
        onChange={setPage}
      />
    </div>
  );
};
```

### 2. Caching Strategy
**Implement client-side caching**:
```javascript
// React Query configuration for caching
import { QueryClient, QueryClientProvider } from 'react-query';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors
        if (error.response?.status >= 400 && error.response?.status < 500) {
          return false;
        }
        return failureCount < 3;
      }
    }
  }
});
```

## Migration Checklist

### Immediate (Before Backend Deployment)
- [ ] Update error handling to support new response format
- [ ] Add validation for search parameters
- [ ] Update CORS configuration for development
- [ ] Test OAuth2 flow with new error responses

### Short Term (1-2 weeks)
- [ ] Implement cache-aware data loading
- [ ] Add support for enhanced search parameters
- [ ] Update battle system WebSocket handlers
- [ ] Add client-side input validation

### Medium Term (1 month)
- [ ] Implement React Query for caching
- [ ] Add performance monitoring
- [ ] Update user feedback for validation errors
- [ ] Add support for new battle features

## Testing Requirements

### 1. API Integration Tests
```javascript
// Test error response format
describe('API Error Handling', () => {
  it('should handle standardized error responses', async () => {
    const response = await api.get('/api/invalid-endpoint');
    expect(response.data.success).toBe(false);
    expect(response.data.message).toBeDefined();
    expect(response.data.code).toBeDefined();
  });
});
```

### 2. Authentication Flow Tests
```javascript
// Test token refresh with new format
describe('Authentication', () => {
  it('should refresh tokens correctly', async () => {
    // Mock token refresh response
    mockApi.post('/api/oauth2/refresh').reply(200, {
      success: true,
      data: {
        accessToken: 'new-token',
        refreshToken: 'new-refresh-token'
      }
    });
    
    const result = await refreshToken();
    expect(result.accessToken).toBe('new-token');
  });
});
```

### 3. WebSocket Tests
```javascript
// Test battle WebSocket messages
describe('Battle WebSocket', () => {
  it('should handle enhanced progress messages', () => {
    const mockMessage = {
      roomId: 1,
      participants: [
        { userId: 1, isActive: true, currentScore: 100 }
      ]
    };
    
    // Test message handling
    handleBattleProgress(mockMessage);
    expect(battleState.participants[0].isActive).toBe(true);
  });
});
```

## Deployment Coordination

### 1. Backend-First Deployment
- Backend changes are backward compatible
- Frontend can be updated gradually
- No immediate breaking changes

### 2. Feature Flags
Consider implementing feature flags for:
- Enhanced search functionality
- New error handling format
- Cache-aware loading

### 3. Rollback Plan
- Keep old error handling as fallback
- Monitor API response formats
- Have rollback procedures for both frontend and backend

## Summary

The backend changes are largely additive and maintain backward compatibility. The main frontend updates needed are:

1. **Error Handling**: Update to handle new standardized error format
2. **Validation**: Add client-side validation to match server-side rules
3. **Caching**: Implement cache-aware data loading
4. **Security**: Update CORS and authentication handling

**Risk Level**: 🟡 **LOW-MEDIUM** - Changes are mostly additive with some error handling updates needed.

**Recommendation**: Frontend updates can be deployed gradually after backend deployment.