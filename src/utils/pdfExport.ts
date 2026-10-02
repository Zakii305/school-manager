import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import {
  Invoice, Student, SchoolBranding, PaymentReceipt,
  PayrollItem, Expense, ClassItem, GradeBand, GradeEntry,
  TimetableSlot, PeriodTiming
} from '../types';

/** Helper to format currency numbers cleanly */
const fmt = (n: number) => `Rs ${Math.round(n).toLocaleString()}`;

/** Helper to add standard school header to any PDF */
function addSchoolHeader(doc: jsPDF, branding: SchoolBranding, title: string, subtitle?: string) {
  const pageWidth = doc.internal.pageSize.getWidth();
  
  // Dark violet header banner
  doc.setFillColor(74, 20, 140); // #4A148C
  doc.rect(0, 0, pageWidth, 28, 'F');

  // School Name
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(13);
  doc.text(branding.name.toUpperCase(), 14, 11);

  // School contact & subtitle
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(8);
  doc.setTextColor(255, 213, 79); // Golden
  doc.text(branding.tagline || 'Excellence in Education • School Management System', 14, 17);

  doc.setTextColor(230, 230, 255);
  doc.text(`${branding.address} | Ph: ${branding.phone} | ${branding.email}`, 14, 22);

  // Right side date & session tag
  doc.setFontSize(8);
  doc.setTextColor(255, 255, 255);
  doc.text(`Academic Session: ${branding.session}`, pageWidth - 14, 11, { align: 'right' });
  doc.text(`Date: ${new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}`, pageWidth - 14, 17, { align: 'right' });

  // Document Title below header
  doc.setTextColor(15, 23, 42); // slate-900
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(13);
  doc.text(title, 14, 38);

  if (subtitle) {
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8.5);
    doc.setTextColor(100, 116, 139); // slate-500
    doc.text(subtitle, 14, 43);
  }

  doc.setDrawColor(226, 232, 240); // slate-200
  doc.line(14, 46, pageWidth - 14, 46);
}

/** Helper to add official footer and page numbers */
function addSchoolFooter(doc: jsPDF, branding: SchoolBranding) {
  const pageCount = (doc as any).internal.getNumberOfPages();
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();

  for (let i = 1; i <= pageCount; i++) {
    doc.setPage(i);
    doc.setDrawColor(226, 232, 240);
    doc.line(14, pageHeight - 16, pageWidth - 14, pageHeight - 16);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8);
    doc.setTextColor(100, 116, 139);
    doc.text(
      `${branding.name} • Official Computer Generated Document • Certified System Record`,
      14,
      pageHeight - 10
    );
    doc.text(`Page ${i} of ${pageCount}`, pageWidth - 14, pageHeight - 10, { align: 'right' });
  }
}

/**
 * 1. STUDENT IDENTITY CARD (Proper Settings: CR80 Card Size 85.6mm x 54mm with Crisp School Styling)
 */
export function exportStudentIDCardPDF(student: Student, branding: SchoolBranding) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: [54, 85.6] });
  const w = 85.6;
  const h = 54;

  // Background
  doc.setFillColor(255, 255, 255);
  doc.rect(0, 0, w, h, 'F');

  // Top School Header Banner
  doc.setFillColor(74, 20, 140); // Deep Violet
  doc.rect(0, 0, w, 14, 'F');

  // School Name
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(8);
  const truncSchool = branding.name.length > 34 ? branding.name.substring(0, 32) + '...' : branding.name;
  doc.text(truncSchool.toUpperCase(), w / 2, 5.5, { align: 'center' });

  // Tagline & Card Type
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(5.5);
  doc.setTextColor(255, 213, 79); // Golden yellow
  doc.text('STUDENT IDENTITY CARD', w / 2, 9, { align: 'center' });

  doc.setFontSize(5);
  doc.setTextColor(230, 230, 255);
  doc.text(`SESSION: ${branding.session}`, w / 2, 12, { align: 'center' });

  // Student Photo Placeholder Box
  doc.setFillColor(241, 245, 249);
  doc.roundedRect(5, 16, 20, 24, 1.5, 1.5, 'F');
  doc.setDrawColor(203, 213, 225);
  doc.roundedRect(5, 16, 20, 24, 1.5, 1.5, 'S');

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(14);
  doc.setTextColor(74, 20, 140);
  doc.text(student.name.charAt(0).toUpperCase(), 15, 29, { align: 'center' });
  doc.setFontSize(4);
  doc.setTextColor(148, 163, 184);
  doc.text('PHOTO', 15, 34, { align: 'center' });

  // Student Details Block
  const textX = 28;
  let textY = 18;

  // Name
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(7.5);
  doc.setTextColor(15, 23, 42);
  doc.text(student.name.toUpperCase(), textX, textY);
  textY += 3.5;

  // Admission & Roll
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(5.5);
  doc.setTextColor(100, 116, 139);
  doc.text('Adm #: ', textX, textY);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(67, 56, 202); // indigo
  doc.text(student.admissionNo, textX + 8, textY);

  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Roll: ', textX + 26, textY);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.rollNo || '01', textX + 31, textY);
  textY += 3.5;

  // Class
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Class: ', textX, textY);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.className, textX + 8, textY);
  textY += 3.5;

  // Father Name
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Father: ', textX, textY);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.fatherName.substring(0, 20), textX + 8, textY);
  textY += 3.5;

  // Phone
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Contact: ', textX, textY);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.phone || branding.phone, textX + 9, textY);
  textY += 3.5;

  // Blood Group Badge
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Blood: ', textX, textY);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(185, 28, 28);
  doc.text(student.bloodGroup || 'O+', textX + 8, textY);

  // Barcode / QR Simulation Pattern
  doc.setDrawColor(30, 41, 59);
  const barY = 41.5;
  const barStartX = 5;
  const barWidths = [1, 0.5, 1.5, 0.8, 0.5, 1.2, 0.6, 1, 0.5, 1.8, 0.5, 1, 0.7, 1.2, 0.5, 1];
  let curBarX = barStartX;
  barWidths.forEach(bw => {
    doc.setLineWidth(bw);
    doc.line(curBarX, barY, curBarX, barY + 4);
    curBarX += bw + 0.8;
  });

  doc.setFontSize(4);
  doc.setTextColor(71, 85, 105);
  doc.text(`*${student.admissionNo}*`, barStartX + 5, barY + 6);

  // Authorized Signature line
  doc.setLineWidth(0.3);
  doc.setDrawColor(100, 116, 139);
  doc.line(w - 28, barY + 4, w - 5, barY + 4);
  doc.setFontSize(4);
  doc.setTextColor(100, 116, 139);
  doc.text('Principal Signature', w - 16.5, barY + 6, { align: 'center' });

  // Bottom address stripe
  doc.setFillColor(74, 20, 140);
  doc.rect(0, 48.5, w, 5.5, 'F');
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(4.2);
  doc.setTextColor(240, 240, 255);
  doc.text(`${branding.address} • Ph: ${branding.phone}`, w / 2, 52, { align: 'center' });

  doc.save(`Student_ID_${student.admissionNo}_${student.name.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 2. CLASS TIMETABLE DOWNLOAD (PDF)
 */
export function exportClassTimetablePDF(
  cls: ClassItem,
  timetableSlots: TimetableSlot[],
  periodTimings: PeriodTiming[],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();

  addSchoolHeader(
    doc,
    branding,
    `CLASS TIMETABLE: ${cls.name.toUpperCase()} - SECTION ${cls.section}`,
    `Class Teacher: ${cls.classTeacherName || 'Assigned Faculty'} | Academic Session: ${branding.session}`
  );

  const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  const headRow = ['Day', ...periodTimings.map(p => `Period ${p.num}\n(${p.time})`)];

  const bodyRows = days.map(day => {
    const row = [day];
    periodTimings.forEach(p => {
      const slot = timetableSlots.find(t => t.classId === cls.id && t.day === day && t.period === p.num);
      row.push(slot ? `${slot.subject}\n(${slot.teacher})` : 'Free Period');
    });
    return row;
  });

  autoTable(doc, {
    startY: 50,
    head: [headRow],
    body: bodyRows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold',
      halign: 'center'
    },
    styles: {
      fontSize: 7.5,
      cellPadding: 3,
      halign: 'center',
      valign: 'middle'
    },
    columnStyles: {
      0: { cellWidth: 26, fontStyle: 'bold', halign: 'left', fillColor: [248, 250, 252] }
    }
  });

  const finalY = (doc as any).lastAutoTable.finalY + 12;
  if (finalY < 180) {
    doc.setFontSize(8);
    doc.setTextColor(100, 116, 139);
    doc.text('Note: Break time is observed between Period 3 and Period 4 (10:00 AM - 10:20 AM).', 14, finalY);

    const signY = finalY + 14;
    doc.setDrawColor(148, 163, 184);
    doc.line(14, signY, 60, signY);
    doc.line(pageWidth - 60, signY, pageWidth - 14, signY);
    doc.text('Class Teacher Signature', 16, signY + 4);
    doc.text('Principal / Vice Principal', pageWidth - 58, signY + 4);
  }

  addSchoolFooter(doc, branding);
  doc.save(`Class_Timetable_${cls.name}_Sec_${cls.section}.pdf`);
}

/**
 * 3. TEACHER'S TIMETABLE DOWNLOAD (PDF)
 */
export function exportTeacherTimetablePDF(
  teacherName: string,
  timetableSlots: TimetableSlot[],
  classes: ClassItem[],
  periodTimings: PeriodTiming[],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();

  addSchoolHeader(
    doc,
    branding,
    `FACULTY WEEKLY TEACHING SCHEDULE: ${teacherName.toUpperCase()}`,
    `Comprehensive timetable of class lectures, subject rooms, and assigned periods.`
  );

  const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  const headRow = ['Day', ...periodTimings.map(p => `Period ${p.num}\n(${p.time})`)];

  const bodyRows = days.map(day => {
    const row = [day];
    periodTimings.forEach(p => {
      const slot = timetableSlots.find(t => t.teacher === teacherName && t.day === day && t.period === p.num);
      if (slot) {
        const cls = classes.find(c => c.id === slot.classId);
        row.push(`${slot.subject}\n[${cls ? `${cls.name} - ${cls.section}` : 'Class'}]`);
      } else {
        row.push('Free Period');
      }
    });
    return row;
  });

  autoTable(doc, {
    startY: 50,
    head: [headRow],
    body: bodyRows,
    theme: 'grid',
    headStyles: {
      fillColor: [16, 149, 106],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold',
      halign: 'center'
    },
    styles: {
      fontSize: 7.5,
      cellPadding: 3,
      halign: 'center',
      valign: 'middle'
    },
    columnStyles: {
      0: { cellWidth: 26, fontStyle: 'bold', halign: 'left', fillColor: [248, 250, 252] }
    }
  });

  const finalY = (doc as any).lastAutoTable.finalY + 12;
  if (finalY < 180) {
    const signY = finalY + 12;
    doc.setDrawColor(148, 163, 184);
    doc.line(14, signY, 60, signY);
    doc.line(pageWidth - 60, signY, pageWidth - 14, signY);
    doc.setFontSize(8);
    doc.setTextColor(100, 116, 139);
    doc.text('Faculty Signature', 16, signY + 4);
    doc.text('Principal / Academic Coordinator', pageWidth - 58, signY + 4);
  }

  addSchoolFooter(doc, branding);
  doc.save(`Teacher_Timetable_${teacherName.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 4. MASTER TIMETABLE CONSOLIDATED (PDF)
 */
export function exportMasterTimetablePDF(
  classes: ClassItem[],
  timetable: TimetableSlot[],
  periods: PeriodTiming[],
  selectedDay: string,
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });

  addSchoolHeader(
    doc,
    branding,
    `INSTITUTIONAL MASTER TIMETABLE (${selectedDay.toUpperCase()})`,
    `Consolidated matrix of all classes, period bell times, and assigned teachers.`
  );

  const headRow = ['Class & Sec', ...periods.map(p => `P${p.num}\n(${p.time})`)];

  const bodyRows = classes.map(c => {
    const row = [`${c.name} - ${c.section}`];
    periods.forEach(p => {
      const slot = timetable.find(t => t.classId === c.id && t.day === selectedDay && t.period === p.num);
      row.push(slot ? `${slot.subject}\n(${slot.teacher})` : 'Free');
    });
    return row;
  });

  autoTable(doc, {
    startY: 50,
    head: [headRow],
    body: bodyRows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 7.5,
      fontStyle: 'bold',
      halign: 'center'
    },
    styles: {
      fontSize: 6.5,
      cellPadding: 2,
      valign: 'middle',
      halign: 'center'
    },
    columnStyles: {
      0: { cellWidth: 26, fontStyle: 'bold', halign: 'left', fillColor: [248, 250, 252] }
    }
  });

  addSchoolFooter(doc, branding);
  doc.save(`Master_Timetable_${selectedDay}_${branding.session}.pdf`);
}

/**
 * 5. STUDENT INDIVIDUAL FEE STATEMENT / LEDGER PDF
 */
export function exportStudentFeeStatementPDF(
  student: Student,
  studentInvoices: Invoice[],
  studentReceipts: PaymentReceipt[],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();

  addSchoolHeader(
    doc,
    branding,
    `STUDENT FEE ACCOUNT STATEMENT`,
    `Itemized ledger of all fee vouchers, dues, concessions, and receipt verifications.`
  );

  // Student Profile Card
  doc.setFillColor(248, 250, 252);
  doc.roundedRect(14, 50, pageWidth - 28, 28, 2, 2, 'F');
  doc.setDrawColor(203, 213, 225);
  doc.roundedRect(14, 50, pageWidth - 28, 28, 2, 2, 'S');

  doc.setFontSize(7.5);
  doc.setTextColor(100, 116, 139);
  doc.text('STUDENT NAME', 18, 56);
  doc.text('ADMISSION #', 80, 56);
  doc.text('ROLL NO', 125, 56);
  doc.text('CLASS & SECTION', 155, 56);

  doc.setFontSize(9.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.name, 18, 61);
  doc.text(`#${student.admissionNo}`, 80, 61);
  doc.text(student.rollNo || '01', 125, 61);
  doc.text(student.className, 155, 61);

  doc.setFontSize(7.5);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('FATHER / GUARDIAN', 18, 68);
  doc.text('CONTACT PHONE', 80, 68);
  doc.text('MONTHLY TUITION', 125, 68);
  doc.text('STATUS', 155, 68);

  doc.setFontSize(9);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.fatherName, 18, 73);
  doc.text(student.phone || '—', 80, 73);
  doc.text(fmt(student.monthlyFee), 125, 73);
  doc.setTextColor(16, 185, 129);
  doc.text(student.status.toUpperCase(), 155, 73);

  // Financial summary row
  const totalBilled = studentInvoices.reduce((s, i) => s + i.total, 0);
  const totalPaid = studentInvoices.reduce((s, i) => s + i.paidAmount, 0);
  const totalBalance = totalBilled - totalPaid;

  const boxY = 82;
  const boxW = (pageWidth - 28 - 8) / 3;

  doc.setFillColor(238, 242, 255);
  doc.roundedRect(14, boxY, boxW, 16, 2, 2, 'F');
  doc.setFontSize(7.5);
  doc.setTextColor(67, 56, 202);
  doc.text('TOTAL AMOUNT BILLED', 18, boxY + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalBilled), 18, boxY + 12);

  doc.setFillColor(236, 253, 245);
  doc.roundedRect(14 + boxW + 4, boxY, boxW, 16, 2, 2, 'F');
  doc.setFontSize(7.5);
  doc.setTextColor(4, 120, 87);
  doc.setFont('helvetica', 'normal');
  doc.text('TOTAL AMOUNT PAID', 14 + boxW + 8, boxY + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalPaid), 14 + boxW + 8, boxY + 12);

  const balBg = totalBalance > 0 ? [254, 242, 242] : [240, 253, 244];
  doc.setFillColor(balBg[0], balBg[1], balBg[2]);
  doc.roundedRect(14 + (boxW + 4) * 2, boxY, boxW, 16, 2, 2, 'F');
  doc.setFontSize(7.5);
  doc.setTextColor(totalBalance > 0 ? 185 : 4, totalBalance > 0 ? 28 : 120, totalBalance > 0 ? 28 : 87);
  doc.setFont('helvetica', 'normal');
  doc.text('OUTSTANDING BALANCE', 14 + (boxW + 4) * 2 + 4, boxY + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalBalance), 14 + (boxW + 4) * 2 + 4, boxY + 12);

  let currentY = boxY + 23;

  doc.setFontSize(10.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text('1. Fee Vouchers & Invoices History', 14, currentY);

  const invoiceRows = studentInvoices.map((inv, idx) => [
    (idx + 1).toString(),
    inv.voucherNo,
    inv.month,
    inv.items.map(it => it.label).join(', '),
    inv.dueDate,
    fmt(inv.total),
    fmt(inv.paidAmount),
    fmt(inv.total - inv.paidAmount),
    inv.status.toUpperCase()
  ]);

  if (invoiceRows.length === 0) {
    invoiceRows.push(['1', 'VCH-REG-01', 'Monthly Fee', 'Tuition Fee', '—', fmt(student.monthlyFee), 'Rs 0', fmt(student.monthlyFee), 'PENDING']);
  }

  autoTable(doc, {
    startY: currentY + 3,
    head: [['#', 'Voucher #', 'Month', 'Particulars', 'Due Date', 'Billed', 'Paid', 'Balance', 'Status']],
    body: invoiceRows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 8, cellPadding: 2.2 },
    columnStyles: {
      0: { cellWidth: 8 },
      1: { cellWidth: 26, fontStyle: 'bold' },
      2: { cellWidth: 22 },
      3: { cellWidth: 42 },
      4: { cellWidth: 20 },
      5: { cellWidth: 18, halign: 'right' },
      6: { cellWidth: 18, halign: 'right' },
      7: { cellWidth: 18, halign: 'right', fontStyle: 'bold' },
      8: { cellWidth: 18, halign: 'center' }
    }
  });

  currentY = (doc as any).lastAutoTable.finalY + 8;
  if (currentY > 230) {
    doc.addPage();
    currentY = 25;
  }

  doc.setFontSize(10.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text('2. Verified Payment Receipts Ledger', 14, currentY);

  const receiptRows = studentReceipts.map((rcpt, idx) => [
    (idx + 1).toString(),
    rcpt.receiptNo,
    rcpt.date,
    rcpt.method,
    rcpt.ref,
    fmt(rcpt.amount),
    rcpt.status,
    rcpt.cashierName
  ]);

  if (receiptRows.length === 0) {
    receiptRows.push(['—', 'No payments logged yet', '—', '—', '—', 'Rs 0', '—', '—']);
  }

  autoTable(doc, {
    startY: currentY + 3,
    head: [['#', 'Receipt #', 'Date', 'Payment Method', 'Bank / TXN Ref', 'Amount Paid', 'Status', 'Verified By']],
    body: receiptRows,
    theme: 'grid',
    headStyles: {
      fillColor: [16, 149, 106],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 8, cellPadding: 2.2 },
    columnStyles: {
      0: { cellWidth: 8 },
      1: { cellWidth: 28, fontStyle: 'bold' },
      2: { cellWidth: 24 },
      3: { cellWidth: 32 },
      4: { cellWidth: 32, fontStyle: 'italic' },
      5: { cellWidth: 22, halign: 'right', fontStyle: 'bold' },
      6: { cellWidth: 20, halign: 'center' },
      7: { cellWidth: 22 }
    }
  });

  addSchoolFooter(doc, branding);
  doc.save(`Fee_Statement_${student.admissionNo}_${student.name.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 6. COMPREHENSIVE SCHOOL FINANCIAL SUMMARY (PDF)
 */
export function exportSchoolFinancialSummaryPDF(
  invoices: Invoice[],
  expenses: Expense[],
  classes: ClassItem[],
  students: Student[],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();

  addSchoolHeader(
    doc,
    branding,
    `INSTITUTIONAL FINANCIAL REPORT & ACCOUNTS SUMMARY`,
    `Revenue collection, outstanding receivables, operating expenses, and net surplus.`
  );

  const totalInvoiced = invoices.reduce((s, i) => s + i.total, 0);
  const totalCollected = invoices.reduce((s, i) => s + i.paidAmount, 0);
  const totalReceivables = totalInvoiced - totalCollected;
  const totalOperatingExpenses = expenses.reduce((s, e) => s + e.amount, 0);
  const netFinancialSurplus = totalCollected - totalOperatingExpenses;

  const kpiY = 50;
  const colW = (pageWidth - 28 - 6) / 3;

  doc.setFillColor(241, 245, 249);
  doc.roundedRect(14, kpiY, colW, 18, 2, 2, 'F');
  doc.setFontSize(7);
  doc.setTextColor(100, 116, 139);
  doc.text('TOTAL REVENUE BILLED', 18, kpiY + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(fmt(totalInvoiced), 18, kpiY + 13);

  doc.setFillColor(236, 253, 245);
  doc.roundedRect(14 + colW + 3, kpiY, colW, 18, 2, 2, 'F');
  doc.setFontSize(7);
  doc.setTextColor(4, 120, 87);
  doc.setFont('helvetica', 'normal');
  doc.text('TOTAL FEES COLLECTED', 14 + colW + 6, kpiY + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalCollected), 14 + colW + 6, kpiY + 13);

  doc.setFillColor(254, 242, 242);
  doc.roundedRect(14 + (colW + 3) * 2, kpiY, colW, 18, 2, 2, 'F');
  doc.setFontSize(7);
  doc.setTextColor(185, 28, 28);
  doc.setFont('helvetica', 'normal');
  doc.text('PENDING RECEIVABLES', 14 + (colW + 3) * 2 + 5, kpiY + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalReceivables), 14 + (colW + 3) * 2 + 5, kpiY + 13);

  const kpi2Y = kpiY + 22;
  const col2W = (pageWidth - 28 - 4) / 2;

  doc.setFillColor(255, 241, 242);
  doc.roundedRect(14, kpi2Y, col2W, 16, 2, 2, 'F');
  doc.setFontSize(7.5);
  doc.setTextColor(225, 29, 72);
  doc.setFont('helvetica', 'normal');
  doc.text('TOTAL OPERATING EXPENSES', 18, kpi2Y + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalOperatingExpenses), 18, kpi2Y + 12);

  const surplusBg = netFinancialSurplus >= 0 ? [240, 253, 244] : [254, 242, 242];
  doc.setFillColor(surplusBg[0], surplusBg[1], surplusBg[2]);
  doc.roundedRect(14 + col2W + 4, kpi2Y, col2W, 16, 2, 2, 'F');
  doc.setFontSize(7.5);
  doc.setTextColor(netFinancialSurplus >= 0 ? 21 : 185, netFinancialSurplus >= 0 ? 128 : 28, netFinancialSurplus >= 0 ? 61 : 28);
  doc.setFont('helvetica', 'normal');
  doc.text('NET OPERATING BALANCE', 14 + col2W + 8, kpi2Y + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(netFinancialSurplus), 14 + col2W + 8, kpi2Y + 12);

  let currentY = kpi2Y + 23;
  doc.setFontSize(10.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text('1. Class-Wise Fee Revenue & Recovery Analysis', 14, currentY);

  const classRows = classes.map((c, idx) => {
    const classInvoices = invoices.filter(i => i.classId === c.id);
    const billed = classInvoices.reduce((s, i) => s + i.total, 0);
    const collected = classInvoices.reduce((s, i) => s + i.paidAmount, 0);
    const pending = billed - collected;
    const rate = billed > 0 ? `${Math.round((collected / billed) * 100)}%` : '0%';

    return [
      (idx + 1).toString(),
      `${c.name} - ${c.section}`,
      c.studentsCount.toString(),
      classInvoices.length.toString(),
      fmt(billed),
      fmt(collected),
      fmt(pending),
      rate
    ];
  });

  autoTable(doc, {
    startY: currentY + 3,
    head: [['#', 'Class & Section', 'Enrolled', 'Vouchers', 'Total Billed', 'Collected', 'Pending', 'Recovery %']],
    body: classRows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 8, cellPadding: 2 },
    columnStyles: {
      0: { cellWidth: 8 },
      1: { cellWidth: 42, fontStyle: 'bold' },
      2: { cellWidth: 18, halign: 'center' },
      3: { cellWidth: 18, halign: 'center' },
      4: { cellWidth: 26, halign: 'right' },
      5: { cellWidth: 26, halign: 'right' },
      6: { cellWidth: 26, halign: 'right', fontStyle: 'bold' },
      7: { cellWidth: 18, halign: 'center', fontStyle: 'bold' }
    }
  });

  currentY = (doc as any).lastAutoTable.finalY + 8;
  if (currentY > 220) {
    doc.addPage();
    currentY = 25;
  }

  doc.setFontSize(10.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text('2. Institutional Expense Vouchers Summary', 14, currentY);

  const expenseRows = expenses.map((exp, idx) => [
    (idx + 1).toString(),
    exp.voucherNo,
    exp.title,
    exp.category,
    exp.paidTo,
    exp.date,
    fmt(exp.amount)
  ]);

  autoTable(doc, {
    startY: currentY + 3,
    head: [['#', 'Voucher #', 'Expense Title', 'Category', 'Paid To / Vendor', 'Date', 'Amount']],
    body: expenseRows,
    theme: 'grid',
    headStyles: {
      fillColor: [190, 18, 60],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 8, cellPadding: 2 },
    columnStyles: {
      0: { cellWidth: 8 },
      1: { cellWidth: 24, fontStyle: 'bold' },
      2: { cellWidth: 54 },
      3: { cellWidth: 26 },
      4: { cellWidth: 32 },
      5: { cellWidth: 20 },
      6: { cellWidth: 22, halign: 'right', fontStyle: 'bold' }
    }
  });

  addSchoolFooter(doc, branding);
  doc.save(`School_Financial_Report_${branding.session.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 7. FEE VOUCHERS LEDGER (PDF)
 */
export function exportInvoicesLedgerPDF(
  invoices: Invoice[],
  branding: SchoolBranding,
  filterTitle: string = 'All Fee Invoices'
) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });

  addSchoolHeader(
    doc,
    branding,
    `FEE VOUCHERS & BILLING LEDGER (${filterTitle.toUpperCase()})`,
    `Detailed record of issued fee vouchers, payable amounts, collected sums, and status.`
  );

  const totalBilled = invoices.reduce((s, i) => s + i.total, 0);
  const totalCollected = invoices.reduce((s, i) => s + i.paidAmount, 0);
  const totalBalance = totalBilled - totalCollected;

  const rows = invoices.map((inv, idx) => [
    (idx + 1).toString(),
    inv.voucherNo,
    inv.studentName,
    inv.className,
    inv.month,
    inv.items.map(it => `${it.label}: ${fmt(it.amount)}`).join(' | '),
    inv.dueDate,
    fmt(inv.total),
    fmt(inv.paidAmount),
    fmt(inv.total - inv.paidAmount),
    inv.status.toUpperCase()
  ]);

  rows.push([
    '',
    'GRAND TOTALS',
    `Total: ${invoices.length} Vouchers`,
    '',
    '',
    '',
    '',
    fmt(totalBilled),
    fmt(totalCollected),
    fmt(totalBalance),
    ''
  ]);

  autoTable(doc, {
    startY: 50,
    head: [['#', 'Voucher #', 'Student Name', 'Class', 'Billing Month', 'Breakdown Particulars', 'Due Date', 'Total Due', 'Paid', 'Balance', 'Status']],
    body: rows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 7.5, cellPadding: 2 },
    columnStyles: {
      0: { cellWidth: 8 },
      1: { cellWidth: 26, fontStyle: 'bold' },
      2: { cellWidth: 38 },
      3: { cellWidth: 24 },
      4: { cellWidth: 24 },
      5: { cellWidth: 64 },
      6: { cellWidth: 20 },
      7: { cellWidth: 22, halign: 'right' },
      8: { cellWidth: 22, halign: 'right' },
      9: { cellWidth: 22, halign: 'right', fontStyle: 'bold' },
      10: { cellWidth: 20, halign: 'center' }
    }
  });

  addSchoolFooter(doc, branding);
  doc.save(`Fee_Invoices_Ledger_${new Date().toISOString().split('T')[0]}.pdf`);
}

/**
 * 8. STAFF PAYROLL STATEMENT (PDF)
 */
export function exportPayrollStatementPDF(
  payroll: PayrollItem[],
  branding: SchoolBranding,
  month: string
) {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });

  addSchoolHeader(
    doc,
    branding,
    `FACULTY & STAFF PAYROLL STATEMENT (${month.toUpperCase()})`,
    `Gross basic salaries, housing/medical allowances (+10%), deductions, and net bank payouts.`
  );

  const totalGross = payroll.reduce((s, p) => s + p.gross, 0);
  const totalAllow = payroll.reduce((s, p) => s + (p.medicalAllowance + p.houseRentAllowance), 0);
  const totalDeduct = payroll.reduce((s, p) => s + (p.taxDeduction + p.absenceDeduction), 0);
  const totalNet = payroll.reduce((s, p) => s + p.net, 0);

  const rows = payroll.map((p, idx) => [
    (idx + 1).toString(),
    p.staffName,
    p.designation,
    fmt(p.gross),
    `+${fmt(p.medicalAllowance + p.houseRentAllowance)}`,
    `-${fmt(p.taxDeduction + p.absenceDeduction)}`,
    fmt(p.net),
    p.status
  ]);

  rows.push([
    '',
    'TOTAL PAYROLL DISBURSEMENT',
    `${payroll.length} Staff Members`,
    fmt(totalGross),
    `+${fmt(totalAllow)}`,
    `-${fmt(totalDeduct)}`,
    fmt(totalNet),
    'APPROVED'
  ]);

  autoTable(doc, {
    startY: 50,
    head: [['#', 'Staff Name', 'Designation', 'Basic Gross', 'Allowances', 'Deductions', 'Net Payable', 'Status']],
    body: rows,
    theme: 'grid',
    headStyles: {
      fillColor: [30, 41, 59],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 8, cellPadding: 2.2 },
    columnStyles: {
      0: { cellWidth: 8 },
      1: { cellWidth: 44, fontStyle: 'bold' },
      2: { cellWidth: 42 },
      3: { cellWidth: 24, halign: 'right' },
      4: { cellWidth: 26, halign: 'right' },
      5: { cellWidth: 26, halign: 'right' },
      6: { cellWidth: 28, halign: 'right', fontStyle: 'bold' },
      7: { cellWidth: 20, halign: 'center' }
    }
  });

  addSchoolFooter(doc, branding);
  doc.save(`Staff_Payroll_${month.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 9. OFFICIAL 3-COPY BANK FEE VOUCHER (PDF)
 */
export function exportFeeVoucherPDF(
  invoice: Invoice,
  student: Student | undefined,
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const copyWidth = (pageWidth - 20) / 3;

  const copies = [
    { title: 'BANK COPY', note: 'Deposit at any authorized bank branch' },
    { title: 'SCHOOL COPY', note: 'Submit to school accounts office' },
    { title: 'STUDENT COPY', note: 'Retain for your personal record' }
  ];

  copies.forEach((copy, i) => {
    const startX = 10 + i * copyWidth;
    const endX = startX + copyWidth - 4;

    doc.setFillColor(74, 20, 140);
    doc.rect(startX, 8, copyWidth - 4, 16, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.text(branding.name.toUpperCase(), startX + (copyWidth - 4) / 2, 13, { align: 'center' });

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(255, 213, 79);
    doc.text(`SESSION: ${branding.session}`, startX + (copyWidth - 4) / 2, 17, { align: 'center' });
    doc.setTextColor(240, 240, 255);
    doc.text(branding.phone, startX + (copyWidth - 4) / 2, 21, { align: 'center' });

    doc.setFillColor(241, 245, 249);
    doc.rect(startX, 26, copyWidth - 4, 7, 'F');
    doc.setTextColor(30, 41, 59);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7.5);
    doc.text(copy.title, startX + (copyWidth - 4) / 2, 30.5, { align: 'center' });

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(7);
    doc.setTextColor(71, 85, 105);

    let y = 38;
    doc.text('Voucher No:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(invoice.voucherNo, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Billing Month:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(invoice.month, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Student Name:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(invoice.studentName, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Admission / Roll #:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(student ? `#${student.admissionNo} (Roll: ${student.rollNo || '01'})` : '—', endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Class & Section:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(invoice.className, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Father Name:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(student?.fatherName || '—', endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Due Date:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(185, 28, 28);
    doc.text(invoice.dueDate, endX - 2, y, { align: 'right' });
    y += 6;

    doc.setDrawColor(203, 213, 225);
    doc.line(startX + 2, y, endX - 2, y);
    y += 4;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7);
    doc.setTextColor(71, 85, 105);
    doc.text('Particulars', startX + 2, y);
    doc.text('Amount (PKR)', endX - 2, y, { align: 'right' });
    y += 4;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(15, 23, 42);
    invoice.items.forEach(it => {
      doc.text(it.label, startX + 2, y);
      doc.text(fmt(it.amount), endX - 2, y, { align: 'right' });
      y += 4.5;
    });

    doc.line(startX + 2, y, endX - 2, y);
    y += 5;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.setTextColor(15, 23, 42);
    doc.text('Total Payable:', startX + 2, y);
    doc.text(fmt(invoice.total), endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFontSize(7);
    doc.setTextColor(4, 120, 87);
    doc.text('Amount Paid:', startX + 2, y);
    doc.text(fmt(invoice.paidAmount), endX - 2, y, { align: 'right' });
    y += 5;

    const remaining = invoice.total - invoice.paidAmount;
    doc.setTextColor(remaining > 0 ? 185 : 4, remaining > 0 ? 28 : 120, remaining > 0 ? 28 : 87);
    doc.text('Net Balance Due:', startX + 2, y);
    doc.text(fmt(remaining), endX - 2, y, { align: 'right' });
    y += 7;

    doc.setFillColor(248, 250, 252);
    doc.rect(startX + 2, y, copyWidth - 8, 16, 'F');
    doc.setFontSize(6);
    doc.setTextColor(71, 85, 105);
    doc.setFont('helvetica', 'bold');
    doc.text('Authorized Deposit Channels:', startX + 4, y + 4);
    doc.setFont('helvetica', 'normal');
    doc.text('Habib Bank (HBL): 0142-7901849201', startX + 4, y + 8);
    doc.text(`Title: ${branding.name.substring(0, 26)}`, startX + 4, y + 11.5);
    doc.text('JazzCash / EasyPaisa: 0300-1234567', startX + 4, y + 15);

    y += 24;
    doc.line(startX + 4, y + 8, startX + (copyWidth / 2) - 8, y + 8);
    doc.line(startX + (copyWidth / 2) + 4, y + 8, endX - 4, y + 8);
    doc.setFontSize(6);
    doc.setTextColor(148, 163, 184);
    doc.text('Cashier / Bank Seal', startX + 8, y + 12);
    doc.text('Authorized Sign', startX + (copyWidth / 2) + 8, y + 12);

    if (i < 2) {
      doc.setDrawColor(203, 213, 225);
      doc.setLineDashPattern([2, 2], 0);
      doc.line(endX + 2, 8, endX + 2, pageHeight - 8);
      doc.setLineDashPattern([], 0);
    }
  });

  doc.save(`Fee_Voucher_${invoice.voucherNo}.pdf`);
}

/**
 * 10. ACADEMIC REPORT CARD (PDF)
 */
export function exportAcademicReportCardPDF(
  student: Student,
  examEntries: GradeEntry[],
  gradeBands: GradeBand[],
  branding: SchoolBranding,
  examName: string = 'Term 1 Mid-Term Examination 2026'
) {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();

  addSchoolHeader(
    doc,
    branding,
    `OFFICIAL ACADEMIC PROGRESS REPORT`,
    `${examName} • Comprehensive Student Evaluation & Grade Sheet`
  );

  doc.setFillColor(248, 250, 252);
  doc.roundedRect(14, 50, pageWidth - 28, 26, 2, 2, 'F');
  doc.setDrawColor(203, 213, 225);
  doc.roundedRect(14, 50, pageWidth - 28, 26, 2, 2, 'S');

  doc.setFontSize(7.5);
  doc.setTextColor(100, 116, 139);
  doc.text('STUDENT NAME', 18, 56);
  doc.text('ADMISSION #', 75, 56);
  doc.text('ROLL NO', 115, 56);
  doc.text('CLASS & SECTION', 145, 56);

  doc.setFontSize(9.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.name, 18, 62);
  doc.text(`#${student.admissionNo}`, 75, 62);
  doc.text(student.rollNo || '01', 115, 62);
  doc.text(student.className, 145, 62);

  doc.setFontSize(7.5);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('FATHER NAME', 18, 68);
  doc.text('SESSION', 75, 68);
  doc.text('EXAM TERM', 115, 68);
  doc.text('ATTENDANCE', 145, 68);

  doc.setFontSize(8.5);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(student.fatherName, 18, 73);
  doc.text(branding.session, 75, 73);
  doc.text('Mid-Term', 115, 73);
  doc.setTextColor(16, 185, 129);
  doc.text('96% (Good Standing)', 145, 73);

  const rows = examEntries.map((e, idx) => {
    const pct = e.total > 0 ? (e.marks / e.total) * 100 : 0;
    const band = gradeBands.find(gb => pct >= gb.minPct && pct <= gb.maxPct) || { grade: 'B', remarks: 'Good' };
    return [
      (idx + 1).toString(),
      e.subject,
      e.total.toString(),
      e.marks.toString(),
      `${Math.round(pct)}%`,
      band.grade,
      band.remarks
    ];
  });

  const totalMarks = examEntries.reduce((s, e) => s + e.marks, 0);
  const totalMax = examEntries.reduce((s, e) => s + e.total, 0);
  const overallPct = totalMax > 0 ? Math.round((totalMarks / totalMax) * 100) : 0;
  const overallBand = gradeBands.find(gb => overallPct >= gb.minPct && overallPct <= gb.maxPct) || { grade: 'B+', remarks: 'Very Good' };

  rows.push([
    '',
    'AGGREGATE TOTAL',
    totalMax.toString(),
    totalMarks.toString(),
    `${overallPct}%`,
    overallBand.grade,
    overallBand.remarks
  ]);

  autoTable(doc, {
    startY: 82,
    head: [['#', 'Subject', 'Max Marks', 'Marks Obtained', 'Percentage', 'Grade', 'Remarks']],
    body: rows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8.5,
      fontStyle: 'bold'
    },
    styles: { fontSize: 8.5, cellPadding: 3 },
    columnStyles: {
      0: { cellWidth: 10 },
      1: { cellWidth: 55, fontStyle: 'bold' },
      2: { cellWidth: 24, halign: 'center' },
      3: { cellWidth: 26, halign: 'center', fontStyle: 'bold' },
      4: { cellWidth: 24, halign: 'center' },
      5: { cellWidth: 18, halign: 'center', fontStyle: 'bold' },
      6: { cellWidth: 35 }
    }
  });

  const currentY = (doc as any).lastAutoTable.finalY + 10;
  const signY = currentY + 30;
  if (signY < 270) {
    doc.setDrawColor(148, 163, 184);
    doc.line(20, signY, 65, signY);
    doc.line(pageWidth / 2 - 25, signY, pageWidth / 2 + 25, signY);
    doc.line(pageWidth - 65, signY, pageWidth - 20, signY);

    doc.setFontSize(8);
    doc.setTextColor(100, 116, 139);
    doc.text('Class Incharge', 28, signY + 4);
    doc.text('Exam Controller', pageWidth / 2 - 12, signY + 4);
    doc.text('Principal / Headmaster', pageWidth - 60, signY + 4);
  }

  addSchoolFooter(doc, branding);
  doc.save(`Result_Card_${student.admissionNo}_${student.name.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 11. GENERIC CUSTOM REPORT EXPORT (Reports Generator PDF)
 */
export function exportCustomReportPDF(
  title: string,
  subtitle: string,
  headers: string[],
  rows: (string | number)[][],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: headers.length > 6 ? 'landscape' : 'portrait', unit: 'mm', format: 'a4' });
  addSchoolHeader(doc, branding, title.toUpperCase(), subtitle);

  autoTable(doc, {
    startY: 50,
    head: [headers],
    body: rows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 7.5, cellPadding: 2.2 }
  });

  addSchoolFooter(doc, branding);
  doc.save(`${title.replace(/[^a-zA-Z0-9]/g, '_')}_${new Date().toISOString().split('T')[0]}.pdf`);
}

/**
 * 12. EXPORT FEE CHALLAN PDF (Wrapper for single invoice voucher)
 */
export function exportFeeChallanPDF(
  invoice: Invoice,
  branding: SchoolBranding,
  student?: Student
) {
  exportFeeVoucherPDF(invoice, student, branding);
}

/**
 * 13. EXPORT FAMILY CHALLAN PDF (Combined Voucher for all Siblings in Family)
 */
export function exportFamilyChallanPDF(
  family: any,
  familyStudents: Student[],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const copyWidth = (pageWidth - 20) / 3;

  const copies = [
    { title: 'BANK COPY', note: 'Deposit at any authorized bank branch' },
    { title: 'SCHOOL COPY', note: 'Submit to school accounts office' },
    { title: 'PARENTS COPY', note: 'Retain for family record' }
  ];

  const totalFamilyDues = familyStudents.reduce((acc, s) => acc + s.monthlyFee, 0);

  copies.forEach((copy, i) => {
    const startX = 10 + i * copyWidth;
    const endX = startX + copyWidth - 4;

    doc.setFillColor(74, 20, 140);
    doc.rect(startX, 8, copyWidth - 4, 16, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.text(branding.name.toUpperCase(), startX + (copyWidth - 4) / 2, 13, { align: 'center' });

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(255, 213, 79);
    doc.text('FAMILY COMBINED CHALLAN', startX + (copyWidth - 4) / 2, 17, { align: 'center' });
    doc.setTextColor(240, 240, 255);
    doc.text(branding.phone, startX + (copyWidth - 4) / 2, 21, { align: 'center' });

    doc.setFillColor(241, 245, 249);
    doc.rect(startX, 26, copyWidth - 4, 7, 'F');
    doc.setTextColor(30, 41, 59);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7.5);
    doc.text(copy.title, startX + (copyWidth - 4) / 2, 30.5, { align: 'center' });

    let y = 38;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(7);
    doc.setTextColor(71, 85, 105);
    doc.text('Family ID:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(family.id, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Family Name:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(family.name, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Father Contact:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(family.fatherPhone, endX - 2, y, { align: 'right' });
    y += 5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(71, 85, 105);
    doc.text('Children Count:', startX + 2, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(15, 23, 42);
    doc.text(`${familyStudents.length} Students`, endX - 2, y, { align: 'right' });
    y += 6;

    doc.setDrawColor(203, 213, 225);
    doc.line(startX + 2, y, endX - 2, y);
    y += 4;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7);
    doc.setTextColor(71, 85, 105);
    doc.text('Student Particulars', startX + 2, y);
    doc.text('Monthly Fee', endX - 2, y, { align: 'right' });
    y += 4.5;

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(15, 23, 42);
    familyStudents.forEach(st => {
      doc.text(`${st.name} (R#${st.rollNo})`, startX + 2, y);
      doc.text(fmt(st.monthlyFee), endX - 2, y, { align: 'right' });
      y += 4.5;
    });

    doc.line(startX + 2, y, endX - 2, y);
    y += 5;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.setTextColor(15, 23, 42);
    doc.text('Total Family Dues:', startX + 2, y);
    doc.text(fmt(totalFamilyDues), endX - 2, y, { align: 'right' });
    y += 8;

    doc.setFillColor(248, 250, 252);
    doc.rect(startX + 2, y, copyWidth - 8, 14, 'F');
    doc.setFontSize(6);
    doc.setTextColor(71, 85, 105);
    doc.setFont('helvetica', 'bold');
    doc.text('Designated Bank Branch:', startX + 4, y + 4);
    doc.setFont('helvetica', 'normal');
    doc.text('HBL Branch: 0142-7901849201', startX + 4, y + 8);
    doc.text(`A/C: ${branding.name.substring(0, 24)}`, startX + 4, y + 11.5);

    y += 24;
    doc.line(startX + 4, y + 4, startX + (copyWidth / 2) - 8, y + 4);
    doc.line(startX + (copyWidth / 2) + 4, y + 4, endX - 4, y + 4);
    doc.setFontSize(6);
    doc.setTextColor(148, 163, 184);
    doc.text('Bank Cashier Seal', startX + 6, y + 8);
    doc.text('Authorized Signature', startX + (copyWidth / 2) + 6, y + 8);

    if (i < 2) {
      doc.setDrawColor(203, 213, 225);
      doc.setLineDashPattern([2, 2], 0);
      doc.line(endX + 2, 8, endX + 2, pageHeight - 8);
      doc.setLineDashPattern([], 0);
    }
  });

  doc.save(`Family_Challan_${family.id}_${family.name.replace(/\s+/g, '_')}.pdf`);
}

/**
 * 14. EXPORT FINANCE REPORT PDF
 */
export function exportFinanceReportPDF(
  invoices: Invoice[],
  receipts: PaymentReceipt[],
  payroll: PayrollItem[],
  expenses: Expense[],
  branding: SchoolBranding
) {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();

  addSchoolHeader(
    doc,
    branding,
    `MONTHLY REVENUE & OPERATING FINANCIAL STATEMENT`,
    `Certified summary of collections, staff salary disbursements, and institutional expenses.`
  );

  const totalCollected = receipts.reduce((acc, r) => acc + r.amount, 0);
  const totalPayroll = payroll.reduce((acc, p) => acc + p.net, 0);
  const totalExpenses = expenses.reduce((acc, e) => acc + e.amount, 0);
  const totalOutflows = totalPayroll + totalExpenses;
  const netBalance = totalCollected - totalOutflows;

  let y = 50;
  const colW = (pageWidth - 28 - 4) / 3;

  doc.setFillColor(236, 253, 245);
  doc.roundedRect(14, y, colW, 16, 2, 2, 'F');
  doc.setFontSize(7);
  doc.setTextColor(4, 120, 87);
  doc.text('TOTAL REVENUE RECEIVED', 18, y + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalCollected), 18, y + 12);

  doc.setFillColor(254, 242, 242);
  doc.roundedRect(14 + colW + 2, y, colW, 16, 2, 2, 'F');
  doc.setFontSize(7);
  doc.setTextColor(185, 28, 28);
  doc.setFont('helvetica', 'normal');
  doc.text('TOTAL EXPENDITURES', 14 + colW + 5, y + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(totalOutflows), 14 + colW + 5, y + 12);

  const netBg = netBalance >= 0 ? [240, 253, 244] : [254, 242, 242];
  doc.setFillColor(netBg[0], netBg[1], netBg[2]);
  doc.roundedRect(14 + (colW + 2) * 2, y, colW, 16, 2, 2, 'F');
  doc.setFontSize(7);
  doc.setTextColor(netBalance >= 0 ? 4 : 185, netBalance >= 0 ? 120 : 28, netBalance >= 0 ? 87 : 28);
  doc.setFont('helvetica', 'normal');
  doc.text('NET OPERATING SURPLUS', 14 + (colW + 2) * 2 + 5, y + 5);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text(fmt(netBalance), 14 + (colW + 2) * 2 + 5, y + 12);

  const receiptRows = receipts.slice(0, 15).map((r, idx) => [
    (idx + 1).toString(),
    r.receiptNo,
    r.studentName,
    r.date,
    r.method.toUpperCase(),
    fmt(r.amount)
  ]);

  autoTable(doc, {
    startY: y + 22,
    head: [['#', 'Receipt #', 'Student', 'Date', 'Channel', 'Amount Paid']],
    body: receiptRows,
    theme: 'grid',
    headStyles: {
      fillColor: [74, 20, 140],
      textColor: [255, 255, 255],
      fontSize: 8,
      fontStyle: 'bold'
    },
    styles: { fontSize: 7.5, cellPadding: 2 }
  });

  addSchoolFooter(doc, branding);
  doc.save(`Finance_Statement_${new Date().toISOString().split('T')[0]}.pdf`);
}
