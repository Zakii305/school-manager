import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { AppMessage, UserRole } from '../../types';
import {
  Mail, Send, Inbox, Trash2, CheckCircle,
  Plus, X, Search, Reply
} from 'lucide-react';

export const MessagesManager: React.FC = () => {
  const {
    currentUser, messages, sendMessage, markMessageAsRead,
    deleteMessage, schools, staff, isOwner
  } = useSchool();

  const [activeFolder, setActiveFolder] = useState<'inbox' | 'sent' | 'compose'>('inbox');
  const [selectedMessage, setSelectedMessage] = useState<AppMessage | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [sendSuccessToast, setSendSuccessToast] = useState<string | null>(null);

  // Compose State
  const [recipientType, setRecipientType] = useState<string>('admin');
  const [customRecipientName, setCustomRecipientName] = useState('School Administration');
  const [subject, setSubject] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState<AppMessage['category']>('general');
  const [priority, setPriority] = useState<AppMessage['priority']>('normal');

  // Filter messages for current user
  const inboxMessages = messages.filter(m => {
    if (m.recipientId === currentUser.uid) return true;
    if (m.recipientId === 'broadcast-all') return true;
    if (currentUser.role === 'admin' && (m.recipientRole === 'admin' || m.recipientId === 'u-admin-1')) return true;
    if (currentUser.role === 'owner' && (m.recipientRole === 'owner' || m.recipientId === 'u-owner-1')) return true;
    if (currentUser.role === 'teacher' && m.recipientId === 'broadcast-teachers') return true;
    if (currentUser.role === 'parent' && m.recipientId === 'broadcast-parents') return true;
    if (currentUser.role === 'student' && m.recipientId === 'broadcast-students') return true;
    return false;
  });

  const sentMessages = messages.filter(m => m.senderId === currentUser.uid || m.senderEmail === currentUser.email);

  const displayedList = (activeFolder === 'inbox' ? inboxMessages : sentMessages).filter(m => {
    const q = searchQuery.toLowerCase();
    const matchesSearch = !q || m.subject.toLowerCase().includes(q) || m.content.toLowerCase().includes(q) || m.senderName.toLowerCase().includes(q) || m.recipientName.toLowerCase().includes(q);
    const matchesCat = categoryFilter === 'all' || m.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  const unreadCount = inboxMessages.filter(m => !m.read).length;

  const handleSelectMessage = (m: AppMessage) => {
    setSelectedMessage(m);
    if (!m.read && activeFolder === 'inbox') {
      markMessageAsRead(m.id);
    }
  };

  const handleOpenCompose = (replyTo?: AppMessage) => {
    if (replyTo) {
      setRecipientType(replyTo.senderId);
      setCustomRecipientName(replyTo.senderName);
      setSubject(`Re: ${replyTo.subject}`);
      setContent(`\n\n--- On ${new Date(replyTo.createdAt).toLocaleDateString()} ${replyTo.senderName} wrote:\n> ${replyTo.content.substring(0, 120)}...`);
      setCategory(replyTo.category);
      setPriority(replyTo.priority);
    } else {
      setRecipientType('admin');
      setCustomRecipientName('School Administration');
      setSubject('');
      setContent('');
      setCategory('general');
      setPriority('normal');
    }
    setActiveFolder('compose');
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!subject.trim() || !content.trim()) {
      alert("Please fill in both the subject and message content.");
      return;
    }

    let recName = customRecipientName;
    let recRole: UserRole | 'all' = 'admin';

    if (recipientType === 'owner') {
      recName = "Sajjad Qasmi (Platform Owner)";
      recRole = 'owner';
    } else if (recipientType === 'admin') {
      recName = "Campus Administrator";
      recRole = 'admin';
    } else if (recipientType === 'broadcast-teachers') {
      recName = "All Faculty Teachers";
      recRole = 'teacher';
    } else if (recipientType === 'broadcast-parents') {
      recName = "All Enrolled Parents";
      recRole = 'parent';
    } else if (recipientType === 'broadcast-all') {
      recName = "All Campus Members";
      recRole = 'all';
    }

    sendMessage({
      senderId: currentUser.uid,
      senderName: currentUser.name,
      senderRole: currentUser.role,
      senderEmail: currentUser.email,
      recipientId: recipientType,
      recipientName: recName,
      recipientRole: recRole,
      schoolId: currentUser.schoolId || schools[0]?.id || "sch-1",
      schoolName: schools[0]?.name || "Sajjad Qasmi Academy",
      subject: subject.trim(),
      content: content.trim(),
      category,
      priority
    });

    setSendSuccessToast(`Message dispatched successfully to ${recName}!`);
    setTimeout(() => setSendSuccessToast(null), 4000);
    setActiveFolder('sent');
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Toast Notification */}
      {sendSuccessToast && (
        <div className="fixed top-20 right-4 z-50 bg-emerald-700 text-white px-4 py-3 rounded-2xl shadow-2xl flex items-center gap-3 animate-in fade-in slide-in-from-top-4">
          <CheckCircle className="w-5 h-5 text-emerald-200" />
          <span className="text-xs font-bold">{sendSuccessToast}</span>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 p-6 rounded-2xl text-white shadow-xl border border-slate-800">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[10px] font-bold uppercase tracking-wider bg-indigo-500 text-white px-2 py-0.5 rounded font-mono">
              INTER-MESSAGING
            </span>
            <span className="text-xs text-indigo-300 font-medium">Logged in as {currentUser.name} ({currentUser.role})</span>
          </div>
          <h1 className="text-xl sm:text-2xl font-black mt-1 flex items-center gap-2">
            <Mail className="w-6 h-6 text-amber-400" />
            <span>Campus & Multi-School Inter-Messaging</span>
          </h1>
          <p className="text-xs text-slate-300 mt-1 max-w-2xl leading-relaxed">
            Direct communication hub between System Owner (Sajjad Qasmi), Campus Administrators, Teachers, Students, and Parents.
          </p>
        </div>

        <button
          onClick={() => handleOpenCompose()}
          className="flex items-center gap-2 px-4 py-2.5 bg-amber-400 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs shadow-md transition shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>Compose New Message</span>
        </button>
      </div>

      {/* Main Mailbox Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* Navigation Sidebar */}
        <div className="lg:col-span-3 space-y-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-3 space-y-1">
            <button
              onClick={() => { setActiveFolder('inbox'); setSelectedMessage(null); }}
              className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl font-bold text-xs transition ${
                activeFolder === 'inbox'
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Inbox className="w-4 h-4" />
                <span>Inbox</span>
              </div>
              {unreadCount > 0 && (
                <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                  activeFolder === 'inbox' ? 'bg-amber-400 text-slate-950' : 'bg-indigo-100 text-indigo-800'
                }`}>
                  {unreadCount}
                </span>
              )}
            </button>

            <button
              onClick={() => { setActiveFolder('sent'); setSelectedMessage(null); }}
              className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl font-bold text-xs transition ${
                activeFolder === 'sent'
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Send className="w-4 h-4" />
                <span>Sent Messages</span>
              </div>
              <span className="text-[10px] opacity-80">{sentMessages.length}</span>
            </button>

            <button
              onClick={() => handleOpenCompose()}
              className={`w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl font-bold text-xs transition ${
                activeFolder === 'compose'
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              <Plus className="w-4 h-4" />
              <span>Compose Message</span>
            </button>
          </div>

          {/* Category Filter */}
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-4 space-y-2">
            <div className="text-[10px] font-bold uppercase text-slate-400 tracking-wider">
              Filter by Category
            </div>
            {['all', 'general', 'academic', 'fees', 'administrative', 'leave'].map(cat => (
              <button
                key={cat}
                onClick={() => setCategoryFilter(cat)}
                className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs font-medium capitalize transition flex items-center justify-between ${
                  categoryFilter === cat ? 'bg-indigo-50 text-indigo-700 font-bold' : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                <span>{cat === 'all' ? 'All Messages' : cat}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Message Content Area */}
        <div className="lg:col-span-9">
          
          {/* FOLDER VIEW: COMPOSE */}
          {activeFolder === 'compose' ? (
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-5 animate-in fade-in">
              <div className="flex items-center justify-between border-b border-slate-200 pb-3">
                <div className="flex items-center gap-2">
                  <Send className="w-5 h-5 text-indigo-600" />
                  <h2 className="text-base font-bold text-slate-900">Compose In-App Message</h2>
                </div>
                <button
                  onClick={() => setActiveFolder('inbox')}
                  className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-700"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleSendMessage} className="space-y-4 text-xs">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Send To Recipient *</label>
                  <select
                    value={recipientType}
                    onChange={e => {
                      setRecipientType(e.target.value);
                      const opt = e.target.options[e.target.selectedIndex];
                      setCustomRecipientName(opt.text);
                    }}
                    className="w-full p-2.5 border border-slate-300 rounded-xl font-medium focus:outline-indigo-500 bg-white"
                  >
                    {isOwner ? (
                      <>
                        <option value="admin">Campus Administrator (Sajjad Qasmi Academy)</option>
                        <option value="broadcast-teachers">All Faculty Teachers (Broadcast)</option>
                        <option value="broadcast-parents">All Parents & Guardians (Broadcast)</option>
                        <option value="broadcast-all">All Campus Members (Platform Broadcast)</option>
                      </>
                    ) : (
                      <>
                        <option value="owner">System Owner: Sajjad Qasmi (socialman121@gmail.com)</option>
                        <option value="admin">School Campus Administration</option>
                        {currentUser.role === 'teacher' && (
                          <>
                            <option value="broadcast-parents">My Class Parents</option>
                            <option value="broadcast-students">My Class Students</option>
                          </>
                        )}
                        {currentUser.role === 'parent' && (
                          <option value="st-7">Class 4-A Teacher: Ms. Hina Zahid</option>
                        )}
                        {currentUser.role === 'student' && (
                          <option value="st-8">Math Teacher: Mr. Bilal Ahmad</option>
                        )}
                      </>
                    )}
                    {staff.slice(0, 5).map(st => (
                      <option key={st.id} value={st.id}>{st.name} ({st.designation})</option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Message Category</label>
                    <select
                      value={category}
                      onChange={e => setCategory(e.target.value as any)}
                      className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500 capitalize bg-white"
                    >
                      <option value="general">General Inquiry / Notice</option>
                      <option value="academic">Academic & Exam Matters</option>
                      <option value="fees">Fee Challan & Accounts</option>
                      <option value="administrative">Administrative & Allotment</option>
                      <option value="leave">Leave & Attendance</option>
                    </select>
                  </div>
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Priority Level</label>
                    <select
                      value={priority}
                      onChange={e => setPriority(e.target.value as any)}
                      className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500 capitalize bg-white"
                    >
                      <option value="normal">Normal Priority</option>
                      <option value="high">High Priority</option>
                      <option value="urgent">Urgent Action Required</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="font-bold text-slate-700 block mb-1">Subject Line *</label>
                  <input
                    type="text"
                    required
                    placeholder="Brief description of the message"
                    value={subject}
                    onChange={e => setSubject(e.target.value)}
                    className="w-full p-2.5 border border-slate-300 rounded-xl font-bold focus:outline-indigo-500"
                  />
                </div>

                <div>
                  <label className="font-bold text-slate-700 block mb-1">Message Body *</label>
                  <textarea
                    rows={6}
                    required
                    placeholder="Type your message here..."
                    value={content}
                    onChange={e => setContent(e.target.value)}
                    className="w-full p-3 border border-slate-300 rounded-xl focus:outline-indigo-500 leading-relaxed font-sans"
                  />
                </div>

                <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-200">
                  <button
                    type="button"
                    onClick={() => setActiveFolder('inbox')}
                    className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-md transition flex items-center gap-2"
                  >
                    <Send className="w-4 h-4" />
                    <span>Send In-App Message</span>
                  </button>
                </div>
              </form>
            </div>
          ) : (
            /* FOLDER VIEW: INBOX OR SENT */
            <div className="space-y-4">
              
              {/* Search & Actions Bar */}
              <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                  <input
                    type="text"
                    placeholder={`Search ${activeFolder === 'inbox' ? 'inbox' : 'sent'} messages...`}
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    className="w-full pl-9 pr-3 py-1.5 border border-slate-300 rounded-xl text-xs focus:outline-indigo-500"
                  />
                </div>
                <div className="text-[11px] font-bold text-slate-500">
                  Showing {displayedList.length} of {activeFolder === 'inbox' ? inboxMessages.length : sentMessages.length} messages
                </div>
              </div>

              {/* Detail View if message selected */}
              {selectedMessage ? (
                <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-4 animate-in fade-in">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                    <button
                      onClick={() => setSelectedMessage(null)}
                      className="text-xs font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1.5"
                    >
                      ← Back to Message List
                    </button>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleOpenCompose(selectedMessage)}
                        className="px-3 py-1.5 rounded-lg bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-xs flex items-center gap-1.5 transition"
                      >
                        <Reply className="w-3.5 h-3.5" />
                        <span>Reply</span>
                      </button>
                      <button
                        onClick={() => {
                          deleteMessage(selectedMessage.id);
                          setSelectedMessage(null);
                        }}
                        className="p-1.5 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition"
                        title="Delete Message"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  <div>
                    <div className="flex flex-wrap items-center gap-2 mb-2">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase font-mono ${
                        selectedMessage.priority === 'urgent' ? 'bg-red-100 text-red-800' :
                        selectedMessage.priority === 'high' ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-700'
                      }`}>
                        {selectedMessage.priority}
                      </span>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-100 text-indigo-800 capitalize">
                        {selectedMessage.category}
                      </span>
                      <span className="text-[11px] text-slate-400">
                        {new Date(selectedMessage.createdAt).toLocaleString()}
                      </span>
                    </div>

                    <h2 className="text-lg font-black text-slate-900 leading-snug">
                      {selectedMessage.subject}
                    </h2>

                    <div className="mt-2 p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <span className="text-slate-500">From: </span>
                        <strong className="text-slate-900">{selectedMessage.senderName}</strong>
                        <span className="text-slate-400 ml-1">({selectedMessage.senderRole}) • {selectedMessage.senderEmail}</span>
                      </div>
                      <div>
                        <span className="text-slate-500">To: </span>
                        <strong className="text-slate-900">{selectedMessage.recipientName}</strong>
                      </div>
                    </div>

                    <div className="mt-4 text-xs text-slate-800 whitespace-pre-wrap leading-relaxed bg-white p-4 rounded-xl border border-slate-100">
                      {selectedMessage.content}
                    </div>
                  </div>
                </div>
              ) : (
                /* List of messages */
                <div className="bg-white rounded-2xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden">
                  {displayedList.length === 0 ? (
                    <div className="p-12 text-center text-xs text-slate-400 space-y-2">
                      <Mail className="w-8 h-8 text-slate-300 mx-auto" />
                      <p>No messages found in this folder.</p>
                      <button
                        onClick={() => handleOpenCompose()}
                        className="text-indigo-600 font-bold hover:underline"
                      >
                        Compose a new message
                      </button>
                    </div>
                  ) : (
                    displayedList.map(msg => (
                      <div
                        key={msg.id}
                        onClick={() => handleSelectMessage(msg)}
                        className={`p-4 hover:bg-indigo-50/40 cursor-pointer transition flex items-start justify-between gap-3 text-xs ${
                          !msg.read && activeFolder === 'inbox' ? 'bg-indigo-50/60 font-semibold' : ''
                        }`}
                      >
                        <div className="space-y-1 flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            {!msg.read && activeFolder === 'inbox' && (
                              <span className="w-2 h-2 rounded-full bg-indigo-600 shrink-0" />
                            )}
                            <span className="font-bold text-slate-900 truncate">
                              {activeFolder === 'inbox' ? msg.senderName : `To: ${msg.recipientName}`}
                            </span>
                            <span className="text-[10px] text-slate-400 font-mono">
                              ({activeFolder === 'inbox' ? msg.senderRole : msg.recipientRole})
                            </span>
                            <span className={`text-[9px] font-bold px-1.5 py-0.2 rounded uppercase ${
                              msg.priority === 'urgent' ? 'bg-red-100 text-red-700' :
                              msg.priority === 'high' ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-600'
                            }`}>
                              {msg.priority}
                            </span>
                          </div>
                          <div className="text-slate-800 text-xs font-medium truncate">
                            {msg.subject}
                          </div>
                          <div className="text-[11px] text-slate-500 line-clamp-1">
                            {msg.content}
                          </div>
                        </div>

                        <div className="text-right shrink-0 space-y-1">
                          <div className="text-[10px] text-slate-400">
                            {new Date(msg.createdAt).toLocaleDateString()}
                          </div>
                          <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 capitalize">
                            {msg.category}
                          </span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              )}

            </div>
          )}

        </div>
      </div>
    </div>
  );
};
