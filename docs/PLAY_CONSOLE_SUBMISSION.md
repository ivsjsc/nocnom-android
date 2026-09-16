# nOcnOm — Google Play Console submission

> Cập nhật theo Android source hiện tại. Phải rà lại Data Safety nếu thêm Analytics, Crashlytics, Ads, Health Connect, location, camera hoặc SDK bên thứ ba mới.

## Identity

- App name: `nOcnOm`
- Package / Application ID: `vn.ivsjsc.nocnom`
- Category đề xuất: Health & Fitness
- Distribution: Free
- Version hiện tại: `0.1.0` (`versionCode 1`)

## Store listing — Vietnamese

### Short description

`Lên kế hoạch bữa ăn, theo dõi calories và macro theo mục tiêu mỗi ngày.`

### Full description

`nOcnOm giúp bạn tổ chức việc ăn uống hằng ngày theo cách rõ ràng và dễ sử dụng.`

`Bạn có thể lập kế hoạch bữa ăn, quản lý kho món, theo dõi calories đã tiêu thụ so với mục tiêu, xem Protein/Carb/Fat khi món ăn có đủ dữ liệu macro và xem lại lịch sử ăn theo ngày.`

`Các chức năng chính:`

- `Kế hoạch bữa ăn theo ngày, bao gồm tùy chọn không ăn buổi trưa.`
- `Kho món và tìm kiếm món ăn.`
- `Calories đã tiêu thụ và mục tiêu calories/ngày.`
- `Theo dõi Protein, Carb và Fat từ dữ liệu macro thực tế của món.`
- `Chỉ số sức khỏe và mục tiêu cá nhân.`
- `Lịch sử ăn theo ngày.`
- `Đăng nhập bằng Email/Password hoặc Google.`
- `Đồng bộ dữ liệu tài khoản qua Firebase/Firestore.`

`nOcnOm không tự suy Protein/Carb/Fat từ calories khi món chưa có đủ dữ liệu macro. Dữ liệu dinh dưỡng có tính chất hỗ trợ theo dõi và tham khảo, không thay thế tư vấn y tế hoặc dinh dưỡng chuyên môn.`

## App access

Ứng dụng yêu cầu đăng nhập để sử dụng dữ liệu cá nhân thật. Khi gửi review, cung cấp tài khoản test hợp lệ nếu Play Console yêu cầu reviewer credentials.

## Current permissions

AndroidManifest hiện chỉ khai báo:

- `android.permission.INTERNET`

Không khai báo location, camera, microphone, contacts, SMS hoặc storage permission.

## Data Safety working matrix

Dựa trên source Android hiện tại:

### Account / Personal information

Firebase Authentication có thể xử lý:

- email;
- Firebase UID;
- tên/ảnh hồ sơ nếu đăng nhập Google cung cấp các trường này.

Profile Firestore có thể chứa:

- họ tên;
- trường/khoa/mã học viên;
- số điện thoại;
- URL ảnh;
- giới tính.

Mục đích: authentication, account management và cung cấp chức năng ứng dụng.

### Health & fitness related data

Profile/meal data có thể chứa:

- chiều cao;
- cân nặng;
- mức vận động;
- mục tiêu sức khỏe;
- calories mục tiêu;
- mục tiêu Protein/Carb/Fat;
- lịch sử món ăn và dữ liệu dinh dưỡng.

Mục đích: cung cấp chức năng theo dõi calories, macro và kế hoạch ăn uống.

### Meal / app content

Firestore có thể lưu:

- món đã chọn;
- danh mục món;
- quán/vendor;
- giá;
- thời điểm ăn;
- calories và macro của log ăn.

Mục đích: cung cấp lịch sử, kế hoạch và đồng bộ dữ liệu giữa thiết bị/tài khoản.

## Not currently present in Android source

Chưa thấy source Android hiện tại khai báo:

- quảng cáo;
- location;
- camera/microphone;
- contacts/SMS;
- Health Connect;
- analytics/crash reporting SDK.

Không khai các mục này trong Data Safety trừ khi source thay đổi trước khi phát hành.

## Release path

1. Tạo app `nOcnOm` trong Play Console.
2. Tạo Play Upload Key bằng `scripts/create-upload-key.ps1`.
3. Thêm SHA-1 và SHA-256 của Upload Key vào Firebase Android app `vn.ivsjsc.nocnom`.
4. Tải lại `google-services.json`, cập nhật GitHub Secret nếu Firebase config thay đổi.
5. Thêm GitHub signing secrets.
6. Chạy workflow `Android Release AAB`.
7. Upload `app-release.aab` vào Internal testing trước.
8. Test auth + Firestore + meal/nutrition flows từ bản cài qua Google Play.
9. Hoàn thiện Store listing, Data Safety, App access, Content rating, Target audience và Privacy Policy.
10. Chỉ Promote lên Production sau khi Internal testing đạt.

## Mandatory release gate

Không đưa Production nếu một trong các điều kiện sau chưa đạt:

- CI/unit tests fail;
- Firebase config không đúng `cocoa-35632` / `vn.ivsjsc.nocnom`;
- release AAB chưa được ký bằng Upload Key;
- Email/Password auth chưa test;
- Google Sign-In chưa test với SHA certificate thực tế;
- Firestore không đọc đúng dữ liệu tài khoản;
- chưa có Privacy Policy URL công khai;
- Play Console còn blocking declaration/task.
