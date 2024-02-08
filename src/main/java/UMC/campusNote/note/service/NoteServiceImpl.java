package UMC.campusNote.note.service;

import UMC.campusNote.common.code.status.ErrorStatus;
import UMC.campusNote.common.exception.GeneralException;
import UMC.campusNote.lesson.entity.Lesson;
import UMC.campusNote.lesson.repository.LessonRepository;
import UMC.campusNote.mapping.UserLesson;
import UMC.campusNote.mapping.UserLessonNote;
import UMC.campusNote.mapping.repository.UserLessonNoteRepository;
import UMC.campusNote.mapping.repository.UserLessonRepository;
import UMC.campusNote.note.entity.Note;
import UMC.campusNote.note.repository.NoteRepository;
import UMC.campusNote.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static UMC.campusNote.classSideNote.status.ClassSideNoteErrorStatus.USER_LESSON_NOT_FOUND;
import static UMC.campusNote.common.code.status.ErrorStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl {

    private final UserLessonRepository userLessonRepository;
    private final NoteRepository noteRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonNoteRepository userLessonNoteRepository;

    @Transactional
    public void createUserNote(User user, Long lessonId, String semester,String noteName) {
        UserLesson userLesson = getUserLesson(user, lessonId, semester);
        createNote(noteName, userLesson);
    }



    private UserLesson getUserLesson(User user, Long lessonId, String semester) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                () -> new GeneralException(LESSON_NOT_FOUND));
        return userLessonRepository.findByUserAndAndAttendedSemesterAndAndLesson(user, semester, lesson)
                .orElseThrow(() -> new GeneralException(USER_LESSON_NOT_FOUND));
    }

    private UserLessonNote createNote(String noteName, UserLesson userLesson) {
        Note note = Note.builder()
                .noteName(noteName)
                .build();

        noteRepository.save(note);
        UserLessonNote userLessonNote = UserLessonNote.builder()
                .userLesson(userLesson)
                .note(note)
                .build();

        userLessonNote.setUserLesson(userLesson);
        userLessonNote.setNote(note);
        return userLessonNoteRepository.save(userLessonNote);
    }
}
