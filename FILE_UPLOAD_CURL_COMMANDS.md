# 📁 File Upload/Download - cURL Commands for Postman

## Prerequisites
**Lấy JWT Token với ADMIN role:**
```bash
POST http://localhost:8080/auth/token
Body: { "username": "admin_username", "password": "admin_password" }
```

Copy `token` từ response và thay vào `{{ADMIN_TOKEN}}` ở các lệnh dưới.

---

## 1️⃣ Upload Image (Admin only)

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/image \
  -H "Authorization: Bearer {{ADMIN_TOKEN}}" \
  -F "file=@/path/to/your/image.jpg" \
  -F "subFolder=courses" \
  -F "description=TOEIC Course Cover Image"
```

**Windows PowerShell:**
```powershell
curl.exe -X POST http://localhost:8080/api/v1/files/upload/image `
  -H "Authorization: Bearer {{ADMIN_TOKEN}}" `
  -F "file=@C:\Users\PC\Desktop\image.jpg" `
  -F "subFolder=courses" `
  -F "description=TOEIC Course Cover Image"
```

**Response:**
```json
{
  "code": 1000,
  "message": "Image uploaded successfully",
  "result": {
    "id": "file-uuid-here",
    "fileName": "20251024_143022_image.jpg",
    "originalFileName": "image.jpg",
    "filePath": "images/courses/20251024_143022_image.jpg",
    "fileUrl": "http://localhost:8080/api/v1/files/file-uuid-here",
    "fileType": "IMAGE",
    "fileSize": 245678,
    "contentType": "image/jpeg",
    "description": "TOEIC Course Cover Image",
    "createdAt": "2025-10-24T14:30:22",
    "createdBy": "admin"
  }
}
```

**Available subFolders for images:**
- `courses` - Ảnh khóa học
- `questions` - Ảnh câu hỏi
- `users` - Avatar người dùng
- `general` - Ảnh chung (mặc định)

---

## 2️⃣ Upload Audio (Admin only)

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/audio \
  -H "Authorization: Bearer {{ADMIN_TOKEN}}" \
  -F "file=@/path/to/your/audio.mp3" \
  -F "subFolder=listening" \
  -F "description=Part 1 Listening Audio"
```

**Windows PowerShell:**
```powershell
curl.exe -X POST http://localhost:8080/api/v1/files/upload/audio `
  -H "Authorization: Bearer {{ADMIN_TOKEN}}" `
  -F "file=@C:\Users\PC\Desktop\audio.mp3" `
  -F "subFolder=listening" `
  -F "description=Part 1 Listening Audio"
```

**Response:**
```json
{
  "code": 1000,
  "message": "Audio uploaded successfully",
  "result": {
    "id": "audio-uuid-here",
    "fileName": "20251024_143530_audio.mp3",
    "originalFileName": "audio.mp3",
    "filePath": "audio/listening/20251024_143530_audio.mp3",
    "fileUrl": "http://localhost:8080/api/v1/files/audio-uuid-here",
    "fileType": "AUDIO",
    "fileSize": 3456789,
    "contentType": "audio/mpeg",
    "description": "Part 1 Listening Audio",
    "createdAt": "2025-10-24T14:35:30",
    "createdBy": "admin"
  }
}
```

**Available subFolders for audio:**
- `listening` - Audio listening test
- `pronunciation` - Audio phát âm
- `conversations` - Hội thoại
- `general` - Audio chung (mặc định)

---

## 3️⃣ Download/Stream File (Public)

```bash
curl -X GET http://localhost:8080/api/v1/files/{{FILE_ID}} \
  --output downloaded-file.jpg
```

**Or open in browser:**
```
http://localhost:8080/api/v1/files/{{FILE_ID}}
```

**Response:** Binary file data (image/audio)

---

## 4️⃣ Check if File Exists (Public)

```bash
curl -X GET http://localhost:8080/api/v1/files/{{FILE_ID}}/exists
```

**Response:**
```json
{
  "code": 1000,
  "message": "File exists",
  "result": true
}
```

---

## 5️⃣ Get All Files by Type (Admin only)

**Get all images:**
```bash
curl -X GET http://localhost:8080/api/v1/files/type/IMAGE \
  -H "Authorization: Bearer {{ADMIN_TOKEN}}"
```

**Get all audio:**
```bash
curl -X GET http://localhost:8080/api/v1/files/type/AUDIO \
  -H "Authorization: Bearer {{ADMIN_TOKEN}}"
```

**Response:**
```json
{
  "code": 1000,
  "message": "Files retrieved successfully",
  "result": [
    {
      "id": "file-uuid-1",
      "fileName": "20251024_143022_image.jpg",
      "fileType": "IMAGE",
      "fileSize": 245678,
      "fileUrl": "http://localhost:8080/api/v1/files/file-uuid-1",
      "description": "Course cover",
      "createdAt": "2025-10-24T14:30:22"
    },
    {
      "id": "file-uuid-2",
      "fileName": "20251024_150000_photo.png",
      "fileType": "IMAGE",
      "fileSize": 512000,
      "fileUrl": "http://localhost:8080/api/v1/files/file-uuid-2",
      "description": "Question image",
      "createdAt": "2025-10-24T15:00:00"
    }
  ]
}
```

---

## 6️⃣ Delete File (Admin only)

```bash
curl -X DELETE http://localhost:8080/api/v1/files/{{FILE_ID}} \
  -H "Authorization: Bearer {{ADMIN_TOKEN}}"
```

**Response:**
```json
{
  "code": 1000,
  "message": "File deleted successfully"
}
```

---

## 📝 File Upload Constraints

### Images
- **Allowed extensions:** jpg, jpeg, png, gif, webp, svg
- **Max size:** 5MB
- **Storage path:** `D:/toeic-storage/images/{subFolder}/`

### Audio
- **Allowed extensions:** mp3, wav, m4a, ogg, aac
- **Max size:** 100MB
- **Storage path:** `D:/toeic-storage/audio/{subFolder}/`

---

## 🧪 Testing với Postman

### Upload Image trong Postman:
1. Chọn method: **POST**
2. URL: `http://localhost:8080/api/v1/files/upload/image`
3. Headers:
   - `Authorization: Bearer {{ADMIN_TOKEN}}`
4. Body: chọn **form-data**
   - Key: `file` (type: **File**) → Chọn file từ máy
   - Key: `subFolder` (type: Text) → Nhập `courses`
   - Key: `description` (type: Text) → Nhập mô tả

### Upload Audio trong Postman:
1. Chọn method: **POST**
2. URL: `http://localhost:8080/api/v1/files/upload/audio`
3. Headers:
   - `Authorization: Bearer {{ADMIN_TOKEN}}`
4. Body: chọn **form-data**
   - Key: `file` (type: **File**) → Chọn file audio
   - Key: `subFolder` (type: Text) → Nhập `listening`
   - Key: `description` (type: Text) → Nhập mô tả

---

## 🔥 Complete Flow Example

### Scenario: Upload ảnh cho khóa học TOEIC

```bash
# 1. Login as admin
POST http://localhost:8080/auth/token
Body: {"username": "admin", "password": "admin123"}

# 2. Upload course cover image
curl.exe -X POST http://localhost:8080/api/v1/files/upload/image `
  -H "Authorization: Bearer eyJhbGc..." `
  -F "file=@C:\Images\toeic-course.jpg" `
  -F "subFolder=courses" `
  -F "description=TOEIC 900+ Course Cover"

# Response → Copy `fileUrl`

# 3. Create course with imageUrl
POST http://localhost:8080/courses
Body: {
  "title": "TOEIC 900+",
  "description": "Complete course",
  "price": 99.99,
  "imageUrl": "http://localhost:8080/api/v1/files/file-uuid-here",
  "duration": 365
}
```

---

## 🎯 Use Cases

### 1. Upload Course Image
```bash
subFolder: courses
description: Course cover for [Course Name]
```

### 2. Upload Question Image
```bash
subFolder: questions
description: Image for question [Question ID]
```

### 3. Upload Listening Audio
```bash
subFolder: listening
description: Audio for Part [X] - Question [Y]
```

### 4. Upload User Avatar
```bash
subFolder: users
description: Avatar for [Username]
```

---

**✅ Ready to upload!** 🚀

**Storage Path:** `D:/toeic-storage/`
- `images/courses/`
- `images/questions/`
- `images/users/`
- `audio/listening/`
- `audio/pronunciation/`
