import {
  Student, Staff, ClassItem, SubjectItem, TimetableSlot,
  Exam, GradeBand, GradeEntry, QuestionPaper,
  SyllabusItem, Invoice, PaymentReceipt,
  PayrollItem, Expense, LibraryBook, TransportRoute,
  Notice, Assignment, LeaveRequest, School,
  Family, StudentAttendance, PeriodTiming, SchoolAsset,
  ParentAccount, RolePermission, SmtpConfig, AcademicSession,
  DemoCredentials, SchoolChangeRequest, RegistrationRequest,
  AppMessage
} from '../types';

export const initialSchools: School[] = [
  {
    id: "sch-1",
    code: "SAQ-01",
    name: "Sajjad Qasmi Academy - Main Campus",
    tagline: "Excellence in Education • Character & Leadership",
    email: "socialman121@gmail.com",
    phone: "+92 300 1234567",
    address: "Housing Colony, Sheikhupura, Punjab, Pakistan",
    currency: "Rs",
    currentSession: "2026-2027",
    status: "active",
    plan: "standard",
    studentCount: 319,
    teacherCount: 24,
    createdAt: Date.now() - 180 * 86400000,
    expiresAt: Date.now() + 365 * 86400000
  },
  {
    id: "sch-2",
    code: "SAQ-02",
    name: "Sajjad Qasmi Model Wing - City Branch",
    tagline: "Inspiring Young Minds • Kindergarten to Grade 5",
    email: "socialman121@gmail.com",
    phone: "+92 300 7654321",
    address: "Civil Lines, Near DHQ Hospital, Sheikhupura",
    currency: "Rs",
    currentSession: "2026-2027",
    status: "active",
    plan: "trial",
    studentCount: 145,
    teacherCount: 12,
    createdAt: Date.now() - 30 * 86400000,
    expiresAt: Date.now() + 60 * 86400000
  }
];

export const initialAcademicSessions: AcademicSession[] = [
  {
    id: "sess-2026",
    label: "2026-2027",
    startDate: "2026-04-01",
    endDate: "2027-03-31",
    isCurrent: true,
    termCount: 3
  },
  {
    id: "sess-2025",
    label: "2025-2026",
    startDate: "2025-04-01",
    endDate: "2026-03-31",
    isCurrent: false,
    termCount: 3
  },
  {
    id: "sess-2027",
    label: "2027-2028",
    startDate: "2027-04-01",
    endDate: "2028-03-31",
    isCurrent: false,
    termCount: 3
  }
];

export const initialDemoCredentials: DemoCredentials[] = [
  {
    role: "owner",
    label: "System Owner (Global)",
    loginId: "socialman121@gmail.com",
    passcode: "Owner@2026",
    notes: "Full unrestricted platform access, multi-school allotment, demo password controls, and global settings."
  },
  {
    role: "admin",
    label: "School Campus Administrator",
    loginId: "admin.saq@school.pk",
    passcode: "Admin@SAQ2026",
    notes: "Campus management, admissions, fee challans, exam marks, and timetable. School-isolated."
  },
  {
    role: "teacher",
    label: "Faculty / Subject Teacher",
    loginId: "teacher.hina@school.pk",
    passcode: "Teach#2026",
    notes: "Assigned class attendance, homework diary, subject marks entry, and leave applications."
  },
  {
    role: "student",
    label: "Enrolled Student",
    loginId: "student.zain@school.pk",
    passcode: "Student#2026",
    notes: "Class timetable, fee vouchers & online pay, exam report cards, and attendance log."
  },
  {
    role: "parent",
    label: "Parent / Guardian",
    loginId: "parent.raza@gmail.com",
    passcode: "Parent#2026",
    notes: "Multi-sibling fee payment, report card downloads, and direct teacher communication."
  }
];

export const initialSmtpConfig: SmtpConfig = {
  host: "smtp.gmail.com",
  port: "587",
  user: "socialman121@gmail.com",
  pass: "sajjad-qasmi-app-pwd-2026",
  senderName: "Sajjad Qasmi Academy Notification Desk",
  senderEmail: "socialman121@gmail.com",
  encryption: "TLS",
  whatsappApiToken: "EAAG_Meta_Cloud_WA_Token_Verified",
  whatsappPhoneId: "1098234871923",
  autoSendFeeSms: true,
  autoSendAttendanceSms: true,
  autoSendExamNotification: true
};

export const initialClasses: ClassItem[] = [
  { id: "c-pg", schoolId: "sch-1", name: "Playgroup", section: "A", sections: ["A", "B"], studentsCount: 28, classTeacherName: "Ms. Fatima Noor", classTeacherId: "st-1" },
  { id: "c-nur", schoolId: "sch-1", name: "Nursery", section: "A", sections: ["A", "B"], studentsCount: 32, classTeacherName: "Ms. Sadia Bibi", classTeacherId: "st-2" },
  { id: "c-prep", schoolId: "sch-1", name: "Prep", section: "A", sections: ["A", "B"], studentsCount: 35, classTeacherName: "Ms. Ayesha Siddiqa", classTeacherId: "st-3" },
  { id: "c-1", schoolId: "sch-1", name: "Class 1", section: "A", sections: ["A", "B"], studentsCount: 40, classTeacherName: "Mr. Imran Tariq", classTeacherId: "st-4" },
  { id: "c-2", schoolId: "sch-1", name: "Class 2", section: "A", sections: ["A", "B"], studentsCount: 38, classTeacherName: "Ms. Bushra Qasim", classTeacherId: "st-5" },
  { id: "c-3", schoolId: "sch-1", name: "Class 3", section: "A", sections: ["A", "B"], studentsCount: 36, classTeacherName: "Mr. Naveed Akhtar", classTeacherId: "st-6" },
  { id: "c-4", schoolId: "sch-1", name: "Class 4", section: "A", sections: ["A", "B"], studentsCount: 34, classTeacherName: "Ms. Hina Zahid", classTeacherId: "st-7" },
  { id: "c-5", schoolId: "sch-1", name: "Class 5", section: "A", sections: ["A", "B"], studentsCount: 30, classTeacherName: "Mr. Bilal Ahmad", classTeacherId: "st-8" },
  { id: "c-9", schoolId: "sch-1", name: "Class 9", section: "A", sections: ["A (Science)", "B (Arts)"], studentsCount: 42, classTeacherName: "Sir Kamran Liaquat", classTeacherId: "st-9" },
  { id: "c-10", schoolId: "sch-1", name: "Class 10", section: "A", sections: ["A (Pre-Medical)", "B (Pre-Eng)"], studentsCount: 44, classTeacherName: "Sir Zafar Iqbal", classTeacherId: "st-10" }
];

export const initialSubjects: SubjectItem[] = [
  { id: "sub-1", schoolId: "sch-1", name: "English Language & Comp", code: "ENG-101", classId: "c-4", className: "Class 4 - A", teacherName: "Ms. Hina Zahid", teacherId: "st-7" },
  { id: "sub-2", schoolId: "sch-1", name: "Mathematics", code: "MTH-102", classId: "c-4", className: "Class 4 - A", teacherName: "Mr. Bilal Ahmad", teacherId: "st-8" },
  { id: "sub-3", schoolId: "sch-1", name: "Urdu Adab", code: "URD-103", classId: "c-4", className: "Class 4 - A", teacherName: "Sir Kamran Liaquat", teacherId: "st-9" },
  { id: "sub-4", schoolId: "sch-1", name: "General Science", code: "SCI-104", classId: "c-4", className: "Class 4 - A", teacherName: "Ms. Sadia Bibi", teacherId: "st-2" },
  { id: "sub-5", schoolId: "sch-1", name: "Islamiat & Quranic Studies", code: "ISL-105", classId: "c-4", className: "Class 4 - A", teacherName: "Qari Muhammad Farooq", teacherId: "st-11" },
  { id: "sub-6", schoolId: "sch-1", name: "Computer Science", code: "CSC-106", classId: "c-4", className: "Class 4 - A", teacherName: "Mr. Naveed Akhtar", teacherId: "st-6" },
  { id: "sub-7", schoolId: "sch-1", name: "Physics", code: "PHY-901", classId: "c-9", className: "Class 9 - A", teacherName: "Sir Zafar Iqbal", teacherId: "st-10" },
  { id: "sub-8", schoolId: "sch-1", name: "Chemistry", code: "CHM-902", classId: "c-9", className: "Class 9 - A", teacherName: "Sir Kamran Liaquat", teacherId: "st-9" },
  { id: "sub-9", schoolId: "sch-1", name: "Biology", code: "BIO-903", classId: "c-9", className: "Class 9 - A", teacherName: "Ms. Bushra Qasim", teacherId: "st-5" },
  { id: "sub-10", schoolId: "sch-1", name: "Pakistan Studies", code: "PST-904", classId: "c-9", className: "Class 9 - A", teacherName: "Mr. Imran Tariq", teacherId: "st-4" }
];

export const initialStaff: Staff[] = [
  { id: "st-1", schoolId: "sch-1", staffId: "ST-101", name: "Ms. Fatima Noor", designation: "Primary Coordinator", department: "Teaching", phone: "0301-2345671", email: "fatima@sajjadqasmi.edu.pk", cnic: "35401-1234567-2", bankAccountNo: "PK82HABB01427901001", bankName: "Habib Bank Ltd", monthlySalary: 52000, hireDate: "2022-03-15", status: "active" },
  { id: "st-2", schoolId: "sch-1", staffId: "ST-102", name: "Ms. Sadia Bibi", designation: "Senior Science Teacher", department: "Teaching", phone: "0302-3456782", email: "sadia@sajjadqasmi.edu.pk", cnic: "35401-2345678-4", bankAccountNo: "PK82HABB01427901002", bankName: "Habib Bank Ltd", monthlySalary: 48000, hireDate: "2021-08-01", status: "active" },
  { id: "st-3", schoolId: "sch-1", staffId: "ST-103", name: "Ms. Ayesha Siddiqa", designation: "Kindergarten Teacher", department: "Teaching", phone: "0303-4567893", email: "ayesha@sajjadqasmi.edu.pk", cnic: "35401-3456789-6", bankAccountNo: "PK82MEZN01928371003", bankName: "Meezan Bank", monthlySalary: 42000, hireDate: "2023-01-10", status: "active" },
  { id: "st-4", schoolId: "sch-1", staffId: "ST-104", name: "Mr. Imran Tariq", designation: "Social Studies Incharge", department: "Teaching", phone: "0304-5678904", email: "imran@sajjadqasmi.edu.pk", cnic: "35401-4567890-1", bankAccountNo: "PK82UBLB09182731004", bankName: "United Bank Ltd", monthlySalary: 50000, hireDate: "2020-09-01", status: "active" },
  { id: "st-5", schoolId: "sch-1", staffId: "ST-105", name: "Ms. Bushra Qasim", designation: "Biology Lecturer", department: "Teaching", phone: "0305-6789015", email: "bushra@sajjadqasmi.edu.pk", cnic: "35401-5678901-2", bankAccountNo: "PK82HABB01427901005", bankName: "Habib Bank Ltd", monthlySalary: 55000, hireDate: "2019-11-15", status: "active" },
  { id: "st-6", schoolId: "sch-1", staffId: "ST-106", name: "Mr. Naveed Akhtar", designation: "IT Lab Incharge", department: "IT", phone: "0306-7890126", email: "naveed@sajjadqasmi.edu.pk", cnic: "35401-6789012-3", bankAccountNo: "PK82HABB01427901006", bankName: "Habib Bank Ltd", monthlySalary: 58000, hireDate: "2021-02-20", status: "active" },
  { id: "st-7", schoolId: "sch-1", staffId: "ST-107", name: "Ms. Hina Zahid", designation: "English Dept Head", department: "Teaching", phone: "0307-8901237", email: "hina@sajjadqasmi.edu.pk", cnic: "35401-7890123-4", bankAccountNo: "PK82HABB01427901007", bankName: "Habib Bank Ltd", monthlySalary: 60000, hireDate: "2018-05-12", status: "active" },
  { id: "st-8", schoolId: "sch-1", staffId: "ST-108", name: "Mr. Bilal Ahmad", designation: "Mathematics Incharge", department: "Teaching", phone: "0308-9012348", email: "bilal@sajjadqasmi.edu.pk", cnic: "35401-8901234-5", bankAccountNo: "PK82BAHL09182371008", bankName: "Bank Al Habib", monthlySalary: 54000, hireDate: "2022-09-01", status: "active" },
  { id: "st-9", schoolId: "sch-1", staffId: "ST-109", name: "Sir Kamran Liaquat", designation: "Vice Principal (Academics)", department: "Administration", phone: "0309-0123459", email: "kamran@sajjadqasmi.edu.pk", cnic: "35401-9012345-7", bankAccountNo: "PK82HABB01427901009", bankName: "Habib Bank Ltd", monthlySalary: 85000, hireDate: "2017-01-01", status: "active" },
  { id: "st-10", schoolId: "sch-1", staffId: "ST-110", name: "Sir Zafar Iqbal", designation: "Senior Physics Faculty", department: "Teaching", phone: "0310-1234560", email: "zafar@sajjadqasmi.edu.pk", cnic: "35401-0123456-9", bankAccountNo: "PK82HABB01427901010", bankName: "Habib Bank Ltd", monthlySalary: 62000, hireDate: "2018-10-15", status: "active" },
  { id: "st-11", schoolId: "sch-1", staffId: "ST-111", name: "Qari Muhammad Farooq", designation: "Nazra & Islamic Tutor", department: "Teaching", phone: "0311-2345671", email: "farooq@sajjadqasmi.edu.pk", cnic: "35401-1122334-1", bankAccountNo: "PK82MEZN01928371011", bankName: "Meezan Bank", monthlySalary: 45000, hireDate: "2020-03-01", status: "active" },
  { id: "st-12", schoolId: "sch-1", staffId: "ST-112", name: "Mr. Tariq Mehmood", designation: "Accountant & Cashier", department: "Accounts", phone: "0312-3456782", email: "accounts@sajjadqasmi.edu.pk", cnic: "35401-2233445-3", bankAccountNo: "PK82HABB01427901012", bankName: "Habib Bank Ltd", monthlySalary: 50000, hireDate: "2020-06-15", status: "active" }
];

export const initialFamilies: Family[] = [
  {
    id: "fam-1",
    schoolId: "sch-1",
    familyCode: "FAM-101",
    name: "Raza Family",
    fatherName: "Muhammad Raza",
    fatherCnic: "35401-8472910-1",
    phone: "0300-8472910",
    address: "House 42, St 5, Housing Colony, Sheikhupura",
    siblingDiscountPercent: 10,
    notes: "Two children enrolled in school. Prefer direct bank deposit.",
    childrenIds: ["stu-1", "stu-2"]
  },
  {
    id: "fam-2",
    schoolId: "sch-1",
    familyCode: "FAM-102",
    name: "Sheikh Family",
    fatherName: "Sheikh Abdul Rehman",
    fatherCnic: "35401-3829104-5",
    phone: "0321-4829104",
    address: "Civil Lines, Near DHQ Hospital, Sheikhupura",
    siblingDiscountPercent: 15,
    notes: "Active in parent-teacher council.",
    childrenIds: ["stu-5", "stu-8"]
  },
  {
    id: "fam-3",
    schoolId: "sch-1",
    familyCode: "FAM-103",
    name: "Aslam Family",
    fatherName: "Muhammad Aslam",
    fatherCnic: "35401-9928172-3",
    phone: "0333-7728192",
    address: "Wapda Town, Block B, Sheikhupura",
    siblingDiscountPercent: 10,
    notes: "Van facility availed for both students.",
    childrenIds: ["stu-3", "stu-7"]
  }
];

export const initialStudents: Student[] = [
  {
    id: "stu-1",
    schoolId: "sch-1",
    admissionNo: "1001",
    rollNo: "01",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Zain Raza",
    nameUrdu: "زین رضا",
    cnicBform: "35401-8472910-3",
    religion: "Islam",
    bloodGroup: "B+",
    dob: "2016-04-12",
    admissionDate: "2021-03-10",
    nationality: "Pakistani",
    gender: "male",
    hafizEQuran: "No",
    address: "House 42, St 5, Housing Colony, Sheikhupura",
    phone: "0300-8472910",
    email: "zain.raza@student.sajjadqasmi.edu.pk",
    fatherName: "Muhammad Raza",
    motherName: "Farzana Raza",
    fatherContact: "0300-8472910",
    motherContact: "0301-8472911",
    fatherOccupation: "Civil Engineer",
    familyId: "fam-1",
    monthlyFee: 3200,
    vanFee: 1200,
    admissionFee: 0,
    examFee: 500,
    remarks: "Bright, consistent in class participation",
    status: "active",
    createdAt: "2021-03-10"
  },
  {
    id: "stu-2",
    schoolId: "sch-1",
    admissionNo: "1002",
    rollNo: "02",
    sessionLabel: "2026-2027",
    classId: "c-2",
    className: "Class 2 - A",
    name: "Ayan Raza",
    nameUrdu: "ایان رضا",
    cnicBform: "35401-8472910-5",
    religion: "Islam",
    bloodGroup: "O+",
    dob: "2018-08-19",
    admissionDate: "2023-03-15",
    nationality: "Pakistani",
    gender: "male",
    hafizEQuran: "No",
    address: "House 42, St 5, Housing Colony, Sheikhupura",
    phone: "0300-8472910",
    email: "ayan.raza@student.sajjadqasmi.edu.pk",
    fatherName: "Muhammad Raza",
    fatherContact: "0300-8472910",
    familyId: "fam-1",
    monthlyFee: 2800,
    vanFee: 1200,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2023-03-15"
  },
  {
    id: "stu-3",
    schoolId: "sch-1",
    admissionNo: "1003",
    rollNo: "03",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Talha Aslam",
    nameUrdu: "طلحہ اسلم",
    cnicBform: "35401-9928172-5",
    religion: "Islam",
    bloodGroup: "A+",
    dob: "2016-01-22",
    admissionDate: "2021-04-01",
    nationality: "Pakistani",
    gender: "male",
    hafizEQuran: "Yes",
    address: "Wapda Town, Block B, Sheikhupura",
    phone: "0333-7728192",
    email: "talha.aslam@student.sajjadqasmi.edu.pk",
    fatherName: "Muhammad Aslam",
    fatherContact: "0333-7728192",
    fatherOccupation: "Businessman",
    familyId: "fam-3",
    monthlyFee: 3200,
    vanFee: 1500,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2021-04-01"
  },
  {
    id: "stu-4",
    schoolId: "sch-1",
    admissionNo: "1004",
    rollNo: "04",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Zain Khan",
    nameUrdu: "زین خان",
    cnicBform: "35401-4412983-1",
    religion: "Islam",
    bloodGroup: "AB+",
    dob: "2016-09-05",
    admissionDate: "2022-02-14",
    nationality: "Pakistani",
    gender: "male",
    hafizEQuran: "No",
    address: "Gujranwala Road, Near Toll Plaza",
    phone: "0345-6677889",
    email: "zain.khan@student.sajjadqasmi.edu.pk",
    fatherName: "Tariq Khan",
    fatherContact: "0345-6677889",
    monthlyFee: 3200,
    vanFee: 0,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2022-02-14"
  },
  {
    id: "stu-5",
    schoolId: "sch-1",
    admissionNo: "1005",
    rollNo: "05",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Sara Sheikh",
    nameUrdu: "سارہ شیخ",
    cnicBform: "35401-3829104-2",
    religion: "Islam",
    bloodGroup: "O-",
    dob: "2016-06-14",
    admissionDate: "2021-03-12",
    nationality: "Pakistani",
    gender: "female",
    hafizEQuran: "No",
    address: "Civil Lines, Near DHQ Hospital, Sheikhupura",
    phone: "0321-4829104",
    email: "sara.sheikh@student.sajjadqasmi.edu.pk",
    fatherName: "Sheikh Abdul Rehman",
    fatherContact: "0321-4829104",
    fatherOccupation: "Chartered Accountant",
    familyId: "fam-2",
    monthlyFee: 3200,
    vanFee: 0,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2021-03-12"
  },
  {
    id: "stu-6",
    schoolId: "sch-1",
    admissionNo: "1006",
    rollNo: "06",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Sobhan Ali",
    nameUrdu: "سبحان علی",
    cnicBform: "35401-5522119-9",
    religion: "Islam",
    bloodGroup: "B-",
    dob: "2016-11-30",
    admissionDate: "2021-05-18",
    nationality: "Pakistani",
    gender: "male",
    hafizEQuran: "No",
    address: "Jinnah Park Colony, Sheikhupura",
    phone: "0302-9988771",
    email: "sobhan.ali@student.sajjadqasmi.edu.pk",
    fatherName: "Zahid Ali",
    fatherContact: "0302-9988771",
    monthlyFee: 3200,
    vanFee: 1200,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2021-05-18"
  },
  {
    id: "stu-7",
    schoolId: "sch-1",
    admissionNo: "1007",
    rollNo: "07",
    sessionLabel: "2026-2027",
    classId: "c-9",
    className: "Class 9 - A",
    name: "Hassan Aslam",
    nameUrdu: "حسن اسلم",
    cnicBform: "35401-9928172-1",
    religion: "Islam",
    bloodGroup: "A+",
    dob: "2011-03-04",
    admissionDate: "2018-04-01",
    nationality: "Pakistani",
    gender: "male",
    hafizEQuran: "No",
    address: "Wapda Town, Block B, Sheikhupura",
    phone: "0333-7728192",
    email: "hassan.aslam@student.sajjadqasmi.edu.pk",
    fatherName: "Muhammad Aslam",
    fatherContact: "0333-7728192",
    familyId: "fam-3",
    monthlyFee: 4500,
    vanFee: 1500,
    admissionFee: 0,
    examFee: 750,
    status: "active",
    createdAt: "2018-04-01"
  },
  {
    id: "stu-8",
    schoolId: "sch-1",
    admissionNo: "1008",
    rollNo: "08",
    sessionLabel: "2026-2027",
    classId: "c-9",
    className: "Class 9 - A",
    name: "Hania Sheikh",
    nameUrdu: "ہانیہ شیخ",
    cnicBform: "35401-3829104-6",
    religion: "Islam",
    bloodGroup: "B+",
    dob: "2011-12-09",
    admissionDate: "2018-03-20",
    nationality: "Pakistani",
    gender: "female",
    hafizEQuran: "No",
    address: "Civil Lines, Near DHQ Hospital, Sheikhupura",
    phone: "0321-4829104",
    email: "hania.sheikh@student.sajjadqasmi.edu.pk",
    fatherName: "Sheikh Abdul Rehman",
    fatherContact: "0321-4829104",
    familyId: "fam-2",
    monthlyFee: 4500,
    vanFee: 0,
    admissionFee: 0,
    examFee: 750,
    status: "active",
    createdAt: "2018-03-20"
  },
  {
    id: "stu-9",
    schoolId: "sch-1",
    admissionNo: "1009",
    rollNo: "09",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Eman Ahmed",
    nameUrdu: "ایمان احمد",
    cnicBform: "35401-7711223-4",
    religion: "Islam",
    bloodGroup: "A-",
    dob: "2016-07-25",
    admissionDate: "2022-08-01",
    nationality: "Pakistani",
    gender: "female",
    hafizEQuran: "No",
    address: "Gulshan-e-Raza, Sheikhupura",
    phone: "0315-7766554",
    email: "eman.ahmed@student.sajjadqasmi.edu.pk",
    fatherName: "Ahmed Nadeem",
    fatherContact: "0315-7766554",
    monthlyFee: 3200,
    vanFee: 1100,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2022-08-01"
  },
  {
    id: "stu-10",
    schoolId: "sch-1",
    admissionNo: "1010",
    rollNo: "10",
    sessionLabel: "2026-2027",
    classId: "c-4",
    className: "Class 4 - A",
    name: "Laiba Khan",
    nameUrdu: "لائبہ خان",
    cnicBform: "35401-1188334-8",
    religion: "Islam",
    bloodGroup: "O+",
    dob: "2016-10-18",
    admissionDate: "2021-09-01",
    nationality: "Pakistani",
    gender: "female",
    hafizEQuran: "No",
    address: "Farooq Ganj, Old City, Sheikhupura",
    phone: "0307-5544332",
    email: "laiba.khan@student.sajjadqasmi.edu.pk",
    fatherName: "Waqas Khan",
    fatherContact: "0307-5544332",
    monthlyFee: 3200,
    vanFee: 0,
    admissionFee: 0,
    examFee: 500,
    status: "active",
    createdAt: "2021-09-01"
  }
];

export const initialTimetable: TimetableSlot[] = [
  // Class 4 - A Monday
  { id: "tt-1", schoolId: "sch-1", classId: "c-4", day: "Monday", period: 1, subject: "Mathematics", teacher: "Mr. Bilal Ahmad", startTime: "08:00", endTime: "08:40" },
  { id: "tt-2", schoolId: "sch-1", classId: "c-4", day: "Monday", period: 2, subject: "English Language & Comp", teacher: "Ms. Hina Zahid", startTime: "08:40", endTime: "09:20" },
  { id: "tt-3", schoolId: "sch-1", classId: "c-4", day: "Monday", period: 3, subject: "Urdu Adab", teacher: "Sir Kamran Liaquat", startTime: "09:20", endTime: "10:00" },
  { id: "tt-4", schoolId: "sch-1", classId: "c-4", day: "Monday", period: 4, subject: "General Science", teacher: "Ms. Sadia Bibi", startTime: "10:20", endTime: "11:00" },
  { id: "tt-5", schoolId: "sch-1", classId: "c-4", day: "Monday", period: 5, subject: "Islamiat & Quranic Studies", teacher: "Qari Muhammad Farooq", startTime: "11:00", endTime: "11:40" },
  { id: "tt-6", schoolId: "sch-1", classId: "c-4", day: "Monday", period: 6, subject: "Computer Science", teacher: "Mr. Naveed Akhtar", startTime: "11:40", endTime: "12:20" },
  // Tuesday
  { id: "tt-7", schoolId: "sch-1", classId: "c-4", day: "Tuesday", period: 1, subject: "English Language & Comp", teacher: "Ms. Hina Zahid", startTime: "08:00", endTime: "08:40" },
  { id: "tt-8", schoolId: "sch-1", classId: "c-4", day: "Tuesday", period: 2, subject: "Mathematics", teacher: "Mr. Bilal Ahmad", startTime: "08:40", endTime: "09:20" },
  { id: "tt-9", schoolId: "sch-1", classId: "c-4", day: "Tuesday", period: 3, subject: "General Science", teacher: "Ms. Sadia Bibi", startTime: "09:20", endTime: "10:00" },
  { id: "tt-10", schoolId: "sch-1", classId: "c-4", day: "Tuesday", period: 4, subject: "Urdu Adab", teacher: "Sir Kamran Liaquat", startTime: "10:20", endTime: "11:00" },
  { id: "tt-11", schoolId: "sch-1", classId: "c-4", day: "Tuesday", period: 5, subject: "Computer Science", teacher: "Mr. Naveed Akhtar", startTime: "11:00", endTime: "11:40" },
  { id: "tt-12", schoolId: "sch-1", classId: "c-4", day: "Tuesday", period: 6, subject: "Art & Library", teacher: "Ms. Fatima Noor", startTime: "11:40", endTime: "12:20" },
  // Wednesday
  { id: "tt-13", schoolId: "sch-1", classId: "c-4", day: "Wednesday", period: 1, subject: "Mathematics", teacher: "Mr. Bilal Ahmad", startTime: "08:00", endTime: "08:40" },
  { id: "tt-14", schoolId: "sch-1", classId: "c-4", day: "Wednesday", period: 2, subject: "General Science", teacher: "Ms. Sadia Bibi", startTime: "08:40", endTime: "09:20" },
  { id: "tt-15", schoolId: "sch-1", classId: "c-4", day: "Wednesday", period: 3, subject: "Urdu Adab", teacher: "Sir Kamran Liaquat", startTime: "09:20", endTime: "10:00" },
  { id: "tt-16", schoolId: "sch-1", classId: "c-4", day: "Wednesday", period: 4, subject: "English Language & Comp", teacher: "Ms. Hina Zahid", startTime: "10:20", endTime: "11:00" },
  { id: "tt-17", schoolId: "sch-1", classId: "c-4", day: "Wednesday", period: 5, subject: "Islamiat & Quranic Studies", teacher: "Qari Muhammad Farooq", startTime: "11:00", endTime: "11:40" },
  { id: "tt-18", schoolId: "sch-1", classId: "c-4", day: "Wednesday", period: 6, subject: "Physical Education", teacher: "Mr. Imran Tariq", startTime: "11:40", endTime: "12:20" },
  // Thursday
  { id: "tt-19", schoolId: "sch-1", classId: "c-4", day: "Thursday", period: 1, subject: "English Language & Comp", teacher: "Ms. Hina Zahid", startTime: "08:00", endTime: "08:40" },
  { id: "tt-20", schoolId: "sch-1", classId: "c-4", day: "Thursday", period: 2, subject: "Mathematics", teacher: "Mr. Bilal Ahmad", startTime: "08:40", endTime: "09:20" },
  { id: "tt-21", schoolId: "sch-1", classId: "c-4", day: "Thursday", period: 3, subject: "Urdu Adab", teacher: "Sir Kamran Liaquat", startTime: "09:20", endTime: "10:00" },
  { id: "tt-22", schoolId: "sch-1", classId: "c-4", day: "Thursday", period: 4, subject: "General Science", teacher: "Ms. Sadia Bibi", startTime: "10:20", endTime: "11:00" },
  { id: "tt-23", schoolId: "sch-1", classId: "c-4", day: "Thursday", period: 5, subject: "Computer Science", teacher: "Mr. Naveed Akhtar", startTime: "11:00", endTime: "11:40" },
  { id: "tt-24", schoolId: "sch-1", classId: "c-4", day: "Thursday", period: 6, subject: "Islamiat & Quranic Studies", teacher: "Qari Muhammad Farooq", startTime: "11:40", endTime: "12:20" },
  // Friday
  { id: "tt-25", schoolId: "sch-1", classId: "c-4", day: "Friday", period: 1, subject: "Islamiat & Quranic Studies", teacher: "Qari Muhammad Farooq", startTime: "08:00", endTime: "08:40" },
  { id: "tt-26", schoolId: "sch-1", classId: "c-4", day: "Friday", period: 2, subject: "Urdu Adab", teacher: "Sir Kamran Liaquat", startTime: "08:40", endTime: "09:20" },
  { id: "tt-27", schoolId: "sch-1", classId: "c-4", day: "Friday", period: 3, subject: "Mathematics", teacher: "Mr. Bilal Ahmad", startTime: "09:20", endTime: "10:00" },
  { id: "tt-28", schoolId: "sch-1", classId: "c-4", day: "Friday", period: 4, subject: "English Language & Comp", teacher: "Ms. Hina Zahid", startTime: "10:20", endTime: "11:00" },
  // Saturday
  { id: "tt-29", schoolId: "sch-1", classId: "c-4", day: "Saturday", period: 1, subject: "General Science", teacher: "Ms. Sadia Bibi", startTime: "08:00", endTime: "08:40" },
  { id: "tt-30", schoolId: "sch-1", classId: "c-4", day: "Saturday", period: 2, subject: "Computer Science Lab", teacher: "Mr. Naveed Akhtar", startTime: "08:40", endTime: "09:20" },
  { id: "tt-31", schoolId: "sch-1", classId: "c-4", day: "Saturday", period: 3, subject: "Weekly Quiz & Review", teacher: "Ms. Hina Zahid", startTime: "09:20", endTime: "10:00" }
];

export const initialGradeBands: GradeBand[] = [
  { id: "gb-1", schoolId: "sch-1", grade: "A+", minPct: 90, maxPct: 100, remarks: "Outstanding & Exceptional Performance" },
  { id: "gb-2", schoolId: "sch-1", grade: "A", minPct: 80, maxPct: 89.99, remarks: "Excellent Academic Achievement" },
  { id: "gb-3", schoolId: "sch-1", grade: "B", minPct: 70, maxPct: 79.99, remarks: "Very Good, Consistent Effort" },
  { id: "gb-4", schoolId: "sch-1", grade: "C", minPct: 60, maxPct: 69.99, remarks: "Good, Shows Steady Progress" },
  { id: "gb-5", schoolId: "sch-1", grade: "D", minPct: 50, maxPct: 59.99, remarks: "Satisfactory, Needs Attention" },
  { id: "gb-6", schoolId: "sch-1", grade: "F", minPct: 0, maxPct: 49.99, remarks: "Needs Improvement / Retest" }
];

export const initialExams: Exam[] = [
  { id: "ex-1", schoolId: "sch-1", sessionId: "sess-2026", name: "Mid-Term Examination 2026", term: "Term 1", startDate: "2026-10-15", endDate: "2026-10-24", maxMarks: 100, passingMarks: 40, status: "ongoing", classId: "c-4", className: "Class 4 - A" },
  { id: "ex-2", schoolId: "sch-1", sessionId: "sess-2026", name: "Mid-Term Examination 2026", term: "Term 1", startDate: "2026-10-15", endDate: "2026-10-24", maxMarks: 100, passingMarks: 40, status: "ongoing", classId: "c-9", className: "Class 9 - A" },
  { id: "ex-3", schoolId: "sch-1", sessionId: "sess-2026", name: "Monthly Assessment - September", term: "Monthly Test", startDate: "2026-09-25", endDate: "2026-09-28", maxMarks: 50, passingMarks: 20, status: "completed", classId: "c-4", className: "Class 4 - A" },
  { id: "ex-4", schoolId: "sch-1", sessionId: "sess-2026", name: "Annual Board Preparation Exam", term: "Send-Up Exam", startDate: "2027-01-10", endDate: "2027-01-20", maxMarks: 100, passingMarks: 40, status: "upcoming", classId: "c-10", className: "Class 10 - A" }
];

export const initialGradeEntries: GradeEntry[] = [
  { id: "ge-1", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-1", subject: "English Language & Comp", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", marks: 94, total: 100 },
  { id: "ge-2", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-2", subject: "Mathematics", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", marks: 98, total: 100 },
  { id: "ge-3", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-3", subject: "Urdu Adab", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", marks: 89, total: 100 },
  { id: "ge-4", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-4", subject: "General Science", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", marks: 92, total: 100 },
  { id: "ge-5", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-5", subject: "Islamiat & Quranic Studies", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", marks: 95, total: 100 },
  { id: "ge-6", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-6", subject: "Computer Science", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", marks: 96, total: 100 },
  { id: "ge-7", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-1", subject: "English Language & Comp", studentId: "stu-5", studentName: "Sara Sheikh", classId: "c-4", marks: 91, total: 100 },
  { id: "ge-8", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-2", subject: "Mathematics", studentId: "stu-5", studentName: "Sara Sheikh", classId: "c-4", marks: 88, total: 100 },
  { id: "ge-9", schoolId: "sch-1", examId: "ex-1", examName: "Mid-Term Examination 2026", subjectId: "sub-3", subject: "Urdu Adab", studentId: "stu-5", studentName: "Sara Sheikh", classId: "c-4", marks: 93, total: 100 }
];

export const initialQuestionPapers: QuestionPaper[] = [
  {
    id: "qp-1",
    schoolId: "sch-1",
    title: "Mathematics Mid-Term Comprehensive Paper",
    subject: "Mathematics",
    className: "Class 4 - A",
    examTerm: "Term 1",
    type: "Objective (20 MCQs) + Subjective (80 Marks)",
    date: "2026-10-15",
    durationMinutes: 150,
    maxMarks: 100,
    passingMarks: 40,
    instructions: "Attempt all questions. Calculators not allowed. Show complete rough work in the margin.",
    topicsCovered: "Fractions, Decimals, Long Division, Factors & Multiples, Angles"
  },
  {
    id: "qp-2",
    schoolId: "sch-1",
    title: "English Language & Essay Writing Paper",
    subject: "English Language & Comp",
    className: "Class 4 - A",
    examTerm: "Term 1",
    type: "Grammar, Comprehension & Creative Essay",
    date: "2026-10-17",
    durationMinutes: 120,
    maxMarks: 100,
    passingMarks: 40,
    instructions: "Write neatly in cursive. Pay strict attention to punctuation and capitalization.",
    topicsCovered: "Tenses, Descriptive Writing, Reading Comprehension, Prepositions"
  },
  {
    id: "qp-3",
    schoolId: "sch-1",
    title: "General Science Theory & Diagram Paper",
    subject: "General Science",
    className: "Class 4 - A",
    examTerm: "Term 1",
    type: "Subjective Theory + Scientific Diagrams",
    date: "2026-10-19",
    durationMinutes: 120,
    maxMarks: 100,
    passingMarks: 40,
    instructions: "Diagrams must be drawn neatly and labeled with pencil only.",
    topicsCovered: "Human Body Systems, Plant Life Cycle, States of Matter, Solar System"
  },
  {
    id: "qp-4",
    schoolId: "sch-1",
    title: "Physics Board Model Examination Paper",
    subject: "Physics",
    className: "Class 9 - A",
    examTerm: "Term 1",
    type: "Theory, Formula Derivations & Numericals",
    date: "2026-10-15",
    durationMinutes: 180,
    maxMarks: 75,
    passingMarks: 25,
    instructions: "Scientific calculators allowed. Write formulas clearly before calculation.",
    topicsCovered: "Physical Quantities, Kinematics, Dynamics, Gravitation"
  }
];

export const initialSyllabus: SyllabusItem[] = [
  { id: "syl-1", schoolId: "sch-1", title: "Term 1 Mathematics Comprehensive Outline", className: "Class 4 - A", subject: "Mathematics", term: "Term 1", chapters: "Chapters 1 to 4: Numbers, Operations, Factors, Prime Numbers" },
  { id: "syl-2", schoolId: "sch-1", title: "General Science Units 1 to 5 (Living Things & Matter)", className: "Class 4 - A", subject: "General Science", term: "Term 1", chapters: "Units 1-5: The Human Body, Animal Classification, Heat & Temperature" },
  { id: "syl-3", schoolId: "sch-1", title: "English Grammar, Comprehension & Creative Writing", className: "Class 4 - A", subject: "English Language & Comp", term: "Term 1", chapters: "Parts of Speech, Active/Passive intro, 3 Composition Essays" },
  { id: "syl-4", schoolId: "sch-1", title: "Physics Matric Part 1 Board Syllabus", className: "Class 9 - A", subject: "Physics", term: "Annual", chapters: "BISE Punjab Board standard syllabus Chapters 1 to 5" }
];

export const initialInvoices: Invoice[] = [
  {
    id: "inv-1",
    schoolId: "sch-1",
    voucherNo: "VCH-2026-1001",
    studentId: "stu-1",
    studentName: "Zain Raza",
    classId: "c-4",
    className: "Class 4 - A",
    month: "October 2026",
    tuitionFee: 3200,
    vanFee: 1200,
    admissionFee: 0,
    examFee: 500,
    arrears: 0,
    discount: 500,
    total: 4400,
    paidAmount: 4400,
    dueDate: "2026-10-10",
    status: "paid",
    createdAt: "2026-10-01",
    items: [
      { label: "Monthly Tuition Fee", amount: 3200 },
      { label: "Van Transport Route 1", amount: 1200 },
      { label: "Term 1 Exam Fund", amount: 500 },
      { label: "Sibling Concession (-10%)", amount: -500 }
    ]
  },
  {
    id: "inv-2",
    schoolId: "sch-1",
    voucherNo: "VCH-2026-1002",
    studentId: "stu-2",
    studentName: "Ayan Raza",
    classId: "c-2",
    className: "Class 2 - A",
    month: "October 2026",
    tuitionFee: 2800,
    vanFee: 1200,
    admissionFee: 0,
    examFee: 500,
    arrears: 0,
    discount: 500,
    total: 4000,
    paidAmount: 4000,
    dueDate: "2026-10-10",
    status: "paid",
    createdAt: "2026-10-01",
    items: [
      { label: "Monthly Tuition Fee", amount: 2800 },
      { label: "Van Transport Route 1", amount: 1200 },
      { label: "Term 1 Exam Fund", amount: 500 },
      { label: "Sibling Concession (-10%)", amount: -500 }
    ]
  },
  {
    id: "inv-3",
    schoolId: "sch-1",
    voucherNo: "VCH-2026-1003",
    studentId: "stu-3",
    studentName: "Talha Aslam",
    classId: "c-4",
    className: "Class 4 - A",
    month: "October 2026",
    tuitionFee: 3200,
    vanFee: 1500,
    admissionFee: 0,
    examFee: 500,
    arrears: 0,
    discount: 0,
    total: 5200,
    paidAmount: 0,
    dueDate: "2026-10-10",
    status: "pending",
    createdAt: "2026-10-01",
    items: [
      { label: "Monthly Tuition Fee", amount: 3200 },
      { label: "Van Transport Route 2", amount: 1500 },
      { label: "Term 1 Exam Fund", amount: 500 }
    ]
  },
  {
    id: "inv-4",
    schoolId: "sch-1",
    voucherNo: "VCH-2026-1004",
    studentId: "stu-4",
    studentName: "Zain Khan",
    classId: "c-4",
    className: "Class 4 - A",
    month: "October 2026",
    tuitionFee: 3200,
    vanFee: 0,
    admissionFee: 0,
    examFee: 500,
    arrears: 0,
    discount: 0,
    total: 3700,
    paidAmount: 0,
    dueDate: "2026-10-10",
    status: "pending",
    createdAt: "2026-10-01",
    items: [
      { label: "Monthly Tuition Fee", amount: 3200 },
      { label: "Term 1 Exam Fund", amount: 500 }
    ]
  },
  {
    id: "inv-5",
    schoolId: "sch-1",
    voucherNo: "VCH-2026-1005",
    studentId: "stu-5",
    studentName: "Sara Sheikh",
    classId: "c-4",
    className: "Class 4 - A",
    month: "October 2026",
    tuitionFee: 3200,
    vanFee: 0,
    admissionFee: 0,
    examFee: 500,
    arrears: 0,
    discount: 0,
    total: 3700,
    paidAmount: 3700,
    dueDate: "2026-10-10",
    status: "paid",
    createdAt: "2026-10-01",
    items: [
      { label: "Monthly Tuition Fee", amount: 3200 },
      { label: "Term 1 Exam Fund", amount: 500 }
    ]
  },
  {
    id: "inv-6",
    schoolId: "sch-1",
    voucherNo: "VCH-2026-1006",
    studentId: "stu-7",
    studentName: "Hassan Aslam",
    classId: "c-9",
    className: "Class 9 - A",
    month: "October 2026",
    tuitionFee: 4500,
    vanFee: 1500,
    admissionFee: 0,
    examFee: 750,
    arrears: 0,
    discount: 0,
    total: 6750,
    paidAmount: 3000,
    dueDate: "2026-10-10",
    status: "partial",
    createdAt: "2026-10-01",
    items: [
      { label: "Monthly Tuition Fee", amount: 4500 },
      { label: "Van Transport Route 2", amount: 1500 },
      { label: "Matric Board Exam Fund", amount: 750 }
    ]
  }
];

export const initialReceipts: PaymentReceipt[] = [
  {
    id: "rcpt-1",
    schoolId: "sch-1",
    receiptNo: "RCPT-2026-9041",
    voucherNo: "VCH-2026-1001",
    studentId: "stu-1",
    studentName: "Zain Raza",
    className: "Class 4 - A",
    amount: 4400,
    method: "Habib Bank Deposit",
    date: "2026-10-01",
    ref: "HBL-DEP-84729102",
    cashierName: "Mr. Tariq Mehmood",
    status: "Approved",
    notes: "Direct bank challan deposit counter verified"
  },
  {
    id: "rcpt-2",
    schoolId: "sch-1",
    receiptNo: "RCPT-2026-9042",
    voucherNo: "VCH-2026-1002",
    studentId: "stu-2",
    studentName: "Ayan Raza",
    className: "Class 2 - A",
    amount: 4000,
    method: "Habib Bank Deposit",
    date: "2026-10-01",
    ref: "HBL-DEP-84729103",
    cashierName: "Mr. Tariq Mehmood",
    status: "Approved",
    notes: "Sibling combined bank payment voucher"
  },
  {
    id: "rcpt-3",
    schoolId: "sch-1",
    receiptNo: "RCPT-2026-9043",
    voucherNo: "VCH-2026-1005",
    studentId: "stu-5",
    studentName: "Sara Sheikh",
    className: "Class 4 - A",
    amount: 3700,
    method: "JazzCash Mobile Wallet",
    date: "2026-10-02",
    ref: "JC-38291044",
    cashierName: "Online Gateway",
    status: "Approved",
    notes: "Instant JazzCash parent app confirmation"
  }
];

export const initialPayroll: PayrollItem[] = [
  { id: "pr-1", schoolId: "sch-1", staffId: "st-1", staffName: "Ms. Fatima Noor", designation: "Primary Coordinator", department: "Teaching", month: "September 2026", bankAccountNo: "PK82HABB01427901001", gross: 52000, medicalAllowance: 5200, houseRentAllowance: 7800, taxDeduction: 1040, absenceDeduction: 0, net: 63960, paymentMode: "Direct Bank Transfer", status: "Paid", disbursedAt: "2026-09-30" },
  { id: "pr-2", schoolId: "sch-1", staffId: "st-2", staffName: "Ms. Sadia Bibi", designation: "Senior Science Teacher", department: "Teaching", month: "September 2026", bankAccountNo: "PK82HABB01427901002", gross: 48000, medicalAllowance: 4800, houseRentAllowance: 7200, taxDeduction: 960, absenceDeduction: 0, net: 59040, paymentMode: "Direct Bank Transfer", status: "Paid", disbursedAt: "2026-09-30" },
  { id: "pr-3", schoolId: "sch-1", staffId: "st-3", staffName: "Ms. Ayesha Siddiqa", designation: "Kindergarten Teacher", department: "Teaching", month: "September 2026", bankAccountNo: "PK82MEZN01928371003", gross: 42000, medicalAllowance: 4200, houseRentAllowance: 6300, taxDeduction: 840, absenceDeduction: 0, net: 51660, paymentMode: "Direct Bank Transfer", status: "Paid", disbursedAt: "2026-09-30" },
  { id: "pr-4", schoolId: "sch-1", staffId: "st-4", staffName: "Mr. Imran Tariq", designation: "Social Studies Incharge", department: "Teaching", month: "September 2026", bankAccountNo: "PK82UBLB09182731004", gross: 50000, medicalAllowance: 5000, houseRentAllowance: 7500, taxDeduction: 1000, absenceDeduction: 0, net: 61500, paymentMode: "Direct Bank Transfer", status: "Paid", disbursedAt: "2026-09-30" },
  { id: "pr-5", schoolId: "sch-1", staffId: "st-9", staffName: "Sir Kamran Liaquat", designation: "Vice Principal (Academics)", department: "Administration", month: "September 2026", bankAccountNo: "PK82HABB01427901009", gross: 85000, medicalAllowance: 8500, houseRentAllowance: 12750, taxDeduction: 1700, absenceDeduction: 0, net: 104550, paymentMode: "Direct Bank Transfer", status: "Paid", disbursedAt: "2026-09-30" }
];

export const initialExpenses: Expense[] = [
  { id: "exp-1", schoolId: "sch-1", voucherNo: "EXP-801", title: "Monthly High-Speed Fiber Internet & Campus WiFi", category: "Utilities & WiFi", amount: 12500, date: "2026-09-28", paidTo: "PTCL Optical Fiber Ltd", paymentMode: "Online Bank", invoiceRef: "PTCL-09-2026", approvedBy: "Principal", notes: "100Mbps dedicated bandwidth for computer lab and administrative wing" },
  { id: "exp-2", schoolId: "sch-1", voucherNo: "EXP-802", title: "Mid-Term Examination Printing & Answer Sheets", category: "Stationery & Printing", amount: 18400, date: "2026-09-25", paidTo: "Oxford Stationery & Print Depot", paymentMode: "Cheque", invoiceRef: "OXP-4482", approvedBy: "Vice Principal", notes: "A4 examination sheets, question paper booklet duplication, mark registers" },
  { id: "exp-3", schoolId: "sch-1", voucherNo: "EXP-803", title: "Standby 50kVA Generator Diesel Fuel Replenishment", category: "Fuel & Generator", amount: 35000, date: "2026-09-22", paidTo: "PSO Service Station Sheikhupura", paymentMode: "Cash", invoiceRef: "PSO-8910", approvedBy: "Admin Officer", notes: "125 liters high-speed diesel for load-shedding power continuity" },
  { id: "exp-4", schoolId: "sch-1", voucherNo: "EXP-804", title: "Science Lab Chemical Reagents & Glass Apparatus", category: "Lab Equipment", amount: 22000, date: "2026-09-15", paidTo: "Al-Razi Scientific Suppliers", paymentMode: "Online Bank", invoiceRef: "ARS-1109", approvedBy: "Principal", notes: "For Class 9 and 10 chemistry and biology matric practical examinations" }
];

export const initialBooks: LibraryBook[] = [
  { id: "bk-1", schoolId: "sch-1", title: "Oxford Progressive English Book 4", author: "Rachel Redford", category: "Textbook", copies: 45, available: 42, shelfNo: "A-01" },
  { id: "bk-2", schoolId: "sch-1", title: "Count Down Mathematics Book 4", author: "Shamim Ahmad", category: "Mathematics", copies: 50, available: 47, shelfNo: "B-03" },
  { id: "bk-3", schoolId: "sch-1", title: "Science Fact File 1 & 2", author: "David Coppock", category: "Science", copies: 35, available: 31, shelfNo: "C-02" },
  { id: "bk-4", schoolId: "sch-1", title: "Stories of the Prophets for Youth", author: "Ibn Kathir (Abbreviated)", category: "Islamic Studies", copies: 25, available: 22, shelfNo: "D-05" },
  { id: "bk-5", schoolId: "sch-1", title: "Fundamentals of Physics Metric", author: "Punjab Textbook Board", category: "Physics", copies: 60, available: 55, shelfNo: "E-01" }
];

export const initialRoutes: TransportRoute[] = [
  { id: "tr-1", schoolId: "sch-1", route: "Route 1: Housing Colony & College Road", vehicleNo: "LES-4412", driver: "Muhammad Asif", driverPhone: "0301-7788991", capacity: 40, monthlyFee: 1200 },
  { id: "tr-2", schoolId: "sch-1", route: "Route 2: Wapda Town, Civil Lines & Hospital Road", vehicleNo: "LES-8821", driver: "Babar Hussain", driverPhone: "0302-6655443", capacity: 45, monthlyFee: 1500 },
  { id: "tr-3", schoolId: "sch-1", route: "Route 3: Gujranwala Road & Toll Plaza Suburbs", vehicleNo: "LES-1290", driver: "Ghulam Rasool", driverPhone: "0303-9988112", capacity: 35, monthlyFee: 1400 }
];

export const initialNotices: Notice[] = [
  { id: "not-1", schoolId: "sch-1", title: "Winter Vacation Schedule 2026 Announced", body: "School will remain closed for winter vacation as per District Education Authority schedule from 24th December. Online assignments and homework diary will remain accessible through the student portal.", createdAt: Date.now() - 2 * 86400000, authorName: "Principal Office", audience: "All Students, Parents & Staff" },
  { id: "not-2", schoolId: "sch-1", title: "Term 1 Parent-Teacher Meeting (PTM)", body: "PTM for Term 1 midterm results is scheduled for this coming Saturday from 9:00 AM to 1:00 PM. Parents will receive printed academic progress cards and review student attendance.", createdAt: Date.now() - 5 * 86400000, authorName: "Vice Principal", audience: "Parents & Teachers" },
  { id: "not-3", schoolId: "sch-1", title: "Annual Inter-School Qirat & Naat Competition", body: "Registration is open for the annual recitation and public speaking competition. Students interested in participation should consult their respective Islamic Studies instructors.", createdAt: Date.now() - 9 * 86400000, authorName: "Coordinator Academics", audience: "All Students" }
];

export const initialAssignments: Assignment[] = [
  {
    id: "asg-1",
    schoolId: "sch-1",
    title: "Mathematics: Fractions & Decimals Problem Set",
    description: "Complete Exercise 4.2 questions 1 through 15 in homework notebooks. Show all working steps clearly.",
    subject: "Mathematics",
    classId: "c-4",
    className: "Class 4 - A",
    dueDate: Date.now() + 4 * 86400000,
    teacherName: "Mr. Bilal Ahmad",
    submissionCount: 14
  },
  {
    id: "asg-2",
    schoolId: "sch-1",
    title: "English: Descriptive Essay on 'My Favorite Season'",
    description: "Write a 150-word essay with good paragraph structure, adjectives, and correct punctuation.",
    subject: "English Language & Comp",
    classId: "c-4",
    className: "Class 4 - A",
    dueDate: Date.now() + 6 * 86400000,
    teacherName: "Ms. Hina Zahid",
    submissionCount: 18
  },
  {
    id: "asg-3",
    schoolId: "sch-1",
    title: "General Science: Diagram of Human Respiratory System",
    description: "Draw and label neatly in the practical notebook with neat colored pencils.",
    subject: "General Science",
    classId: "c-4",
    className: "Class 4 - A",
    dueDate: Date.now() + 2 * 86400000,
    teacherName: "Ms. Sadia Bibi",
    submissionCount: 22
  }
];

export const initialAttendanceRecords: StudentAttendance[] = [
  { id: "att-1", schoolId: "sch-1", studentId: "stu-1", studentName: "Zain Raza", classId: "c-4", date: "2026-10-01", status: "present" },
  { id: "att-2", schoolId: "sch-1", studentId: "stu-2", studentName: "Ayan Raza", classId: "c-2", date: "2026-10-01", status: "present" },
  { id: "att-3", schoolId: "sch-1", studentId: "stu-3", studentName: "Talha Aslam", classId: "c-4", date: "2026-10-01", status: "late" },
  { id: "att-4", schoolId: "sch-1", studentId: "stu-4", studentName: "Zain Khan", classId: "c-4", date: "2026-10-01", status: "present" },
  { id: "att-5", schoolId: "sch-1", studentId: "stu-5", studentName: "Sara Sheikh", classId: "c-4", date: "2026-10-01", status: "absent" }
];

export const initialLeaveRequests: LeaveRequest[] = [
  { id: "lr-1", schoolId: "sch-1", teacherId: "st-7", teacherName: "Ms. Hina Zahid", fromDate: "2026-10-12", toDate: "2026-10-14", reason: "Family wedding event in Lahore", status: "approved", responseNote: "Approved. Syllabus arrangements verified with primary coordinator.", respondedBy: "Principal", respondedAt: Date.now() - 2 * 86400000, createdAt: Date.now() - 3 * 86400000 },
  { id: "lr-2", schoolId: "sch-1", teacherId: "st-8", teacherName: "Mr. Bilal Ahmad", fromDate: "2026-10-20", toDate: "2026-10-21", reason: "Medical appointment / dental procedure", status: "pending", createdAt: Date.now() - 1 * 86400000 }
];

export const initialPeriodTimings: PeriodTiming[] = [
  { num: 1, time: "08:00 - 08:40" },
  { num: 2, time: "08:40 - 09:20" },
  { num: 3, time: "09:20 - 10:00" },
  { num: 4, time: "10:20 - 11:00" },
  { num: 5, time: "11:00 - 11:40" },
  { num: 6, time: "11:40 - 12:20" },
  { num: 7, time: "12:20 - 01:00" },
  { num: 8, time: "01:00 - 01:40" }
];

export const initialAssets: SchoolAsset[] = [
  {
    id: "ast-1",
    schoolId: "sch-1",
    name: "Dell OptiPlex All-in-One Desktop PCs",
    category: "IT Equipment",
    quantity: 25,
    unitValue: 65000,
    totalValue: 1625000,
    location: "Main Computer Lab (Room 12)",
    condition: "Good",
    purchaseDate: "2024-03-15"
  },
  {
    id: "ast-2",
    schoolId: "sch-1",
    name: "Heavy-Duty Ergonomic Student Benches & Desks",
    category: "Furniture",
    quantity: 120,
    unitValue: 4500,
    totalValue: 540000,
    location: "Junior & Senior Classrooms",
    condition: "Good",
    purchaseDate: "2023-08-10"
  },
  {
    id: "ast-3",
    schoolId: "sch-1",
    name: "Epson 4K Interactive Smart Projectors",
    category: "Audio Visual",
    quantity: 6,
    unitValue: 110000,
    totalValue: 660000,
    location: "Senior Wing & Auditorium",
    condition: "New",
    purchaseDate: "2025-01-20"
  },
  {
    id: "ast-4",
    schoolId: "sch-1",
    name: "Physics & Chemistry Lab Compound Microscopes",
    category: "Lab Equipment",
    quantity: 18,
    unitValue: 18500,
    totalValue: 333000,
    location: "Science Laboratory",
    condition: "Good",
    purchaseDate: "2023-11-05"
  }
];

export const initialParentAccounts: ParentAccount[] = [
  {
    id: "pa-1",
    schoolId: "sch-1",
    name: "Muhammad Raza",
    email: "raza.family@gmail.com",
    phone: "0300-8472910",
    childrenIds: ["stu-1", "stu-2"],
    status: "active",
    lastLogin: "Today, 10:15 AM"
  },
  {
    id: "pa-2",
    schoolId: "sch-1",
    name: "Muhammad Aslam",
    email: "aslam.family@hotmail.com",
    phone: "0333-7728192",
    childrenIds: ["stu-3", "stu-7"],
    status: "active",
    lastLogin: "Yesterday, 04:30 PM"
  },
  {
    id: "pa-3",
    schoolId: "sch-1",
    name: "Sheikh Abdul Rehman",
    email: "dr.rehman@clinic.pk",
    phone: "0321-4829104",
    childrenIds: ["stu-5", "stu-8"],
    status: "active",
    lastLogin: "Oct 01, 2026"
  }
];

export const initialRolePermissions: RolePermission[] = [
  {
    id: "rp-admin",
    name: "Campus Administrator",
    description: "Full institutional management, financial oversight, and academic control for assigned school.",
    userCount: 2,
    permissions: {
      canManageStudents: true,
      canManageFees: true,
      canManageExams: true,
      canManageTimetable: true,
      canManagePayroll: true,
      canManageSettings: true
    }
  },
  {
    id: "rp-teacher",
    name: "Faculty / Teacher",
    description: "Class timetable viewing, attendance marking, homework diary, and exam marks entry.",
    userCount: 24,
    permissions: {
      canManageStudents: false,
      canManageFees: false,
      canManageExams: true,
      canManageTimetable: true,
      canManagePayroll: false,
      canManageSettings: false
    }
  },
  {
    id: "rp-accountant",
    name: "Accountant / Cashier",
    description: "Fee vouchers collection, receipts generation, family billing, and expense tracking.",
    userCount: 2,
    permissions: {
      canManageStudents: false,
      canManageFees: true,
      canManageExams: false,
      canManageTimetable: false,
      canManagePayroll: true,
      canManageSettings: false
    }
  }
];

export const initialSchoolChangeRequests: SchoolChangeRequest[] = [
  {
    id: "scr-1",
    schoolId: "sch-1",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    adminEmail: "socialman121@gmail.com",
    requestedChanges: {
      tagline: "Inspiring Innovation & Character Excellence",
      phone: "+92 300 9988776",
      reason: "Updated campus helpdesk contact helpline and official school motto adopted by Governing Board."
    },
    status: "pending",
    createdAt: Date.now() - 86400000
  }
];

export const initialRegistrationRequests: RegistrationRequest[] = [
  {
    id: "reg-1",
    name: "Zubair Hashmi",
    email: "hashmi.parent@yahoo.com",
    phone: "0300-4455667",
    requestedRole: "parent",
    schoolCode: "SAQ-01",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    childAdmissionNo: "1001",
    status: "pending",
    createdAt: Date.now() - 3600000
  },
  {
    id: "reg-2",
    name: "Miss Maria Khan",
    email: "maria.khan@gmail.com",
    phone: "0321-9988112",
    requestedRole: "teacher",
    schoolCode: "SAQ-01",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    status: "pending",
    createdAt: Date.now() - 7200000
  }
];

export const initialMessages: AppMessage[] = [
  {
    id: "msg-1",
    senderId: "u-owner-1",
    senderName: "Sajjad Qasmi (Platform Owner)",
    senderRole: "owner",
    senderEmail: "socialman121@gmail.com",
    recipientId: "broadcast-all",
    recipientName: "All School Campuses & Administrators",
    recipientRole: "all",
    schoolId: "sch-1",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    subject: "Academic Session 2026-2027 Platform Upgrades & Guidelines",
    content: "Assalam-o-Alaikum,\n\nWe have successfully deployed the 2026-2027 institutional management suite across all campuses. All administrators are advised to review the revised fee structures, master timetable allocations, and QR attendance registers. Please ensure all student ID cards and monthly vouchers are generated in high-resolution PDF format.\n\nBest regards,\nSajjad Qasmi\nPlatform Owner & Founder",
    category: "administrative",
    priority: "high",
    createdAt: Date.now() - 3600000 * 24,
    read: true
  },
  {
    id: "msg-2",
    senderId: "u-owner-1",
    senderName: "Sajjad Qasmi",
    senderRole: "owner",
    senderEmail: "socialman121@gmail.com",
    recipientId: "u-admin-1",
    recipientName: "Campus Administrator",
    recipientRole: "admin",
    schoolId: "sch-1",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    subject: "Approval of Campus Expansion & Student Allotments",
    content: "Dear Admin,\n\nYour recent campus update and admissions batch for Class 9 and Class 10 have been verified. You may proceed with issuing biometric and QR cards for the new session.\n\nWarm regards,\nSajjad Qasmi",
    category: "general",
    priority: "normal",
    createdAt: Date.now() - 3600000 * 8,
    read: false
  },
  {
    id: "msg-3",
    senderId: "st-7",
    senderName: "Ms. Hina Zahid (English Dept Head)",
    senderRole: "teacher",
    senderEmail: "hina@sajjadqasmi.edu.pk",
    recipientId: "fam-1",
    recipientName: "Mr. Raza (Father of Zain Raza)",
    recipientRole: "parent",
    schoolId: "sch-1",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    subject: "Class 4-A Academic Performance & Class Participation",
    content: "Respected Mr. Raza,\n\nZain has demonstrated commendable performance in English comprehension and grammar. Please ensure he continues daily reading exercises at home for 20 minutes.\n\nRegards,\nMs. Hina Zahid",
    category: "academic",
    priority: "normal",
    createdAt: Date.now() - 3600000 * 5,
    read: false
  },
  {
    id: "msg-4",
    senderId: "fam-1",
    senderName: "Mr. Raza (Parent)",
    senderRole: "parent",
    senderEmail: "parent.raza@gmail.com",
    recipientId: "u-admin-1",
    recipientName: "School Administration",
    recipientRole: "admin",
    schoolId: "sch-1",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    subject: "Transport Route 1 Morning Pickup Timing Confirmation",
    content: "Dear Administration,\n\nCould you please verify whether Van LES-4821 will stop at Housing Colony Gate 2 at 07:15 AM starting next Monday? Thank you.\n\nRegards,\nRaza",
    category: "general",
    priority: "normal",
    createdAt: Date.now() - 3600000 * 3,
    read: false
  },
  {
    id: "msg-5",
    senderId: "stu-1",
    senderName: "Zain Raza (Roll # 101)",
    senderRole: "student",
    senderEmail: "zain.raza@student.sajjadqasmi.edu.pk",
    recipientId: "st-8",
    recipientName: "Mr. Bilal Ahmad (Mathematics Incharge)",
    recipientRole: "teacher",
    schoolId: "sch-1",
    schoolName: "Sajjad Qasmi Academy - Main Campus",
    subject: "Math Homework Exercise 3.2 Question # 5",
    content: "Sir,\n\nI was reviewing Exercise 3.2 on Fractions and had a question regarding the LCM method in question 5. I will discuss it with you during the first period tomorrow. Thank you sir!",
    category: "academic",
    priority: "normal",
    createdAt: Date.now() - 3600000 * 1,
    read: false
  }
];
