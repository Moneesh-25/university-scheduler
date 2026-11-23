package com.university.scheduler.service;

import com.university.scheduler.dto.ClassroomDto;
import com.university.scheduler.entity.Classroom;
import com.university.scheduler.entity.RoomType;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.ClassroomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ClassroomService(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public List<ClassroomDto> getAllClassrooms() {
        return classroomRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ClassroomDto getClassroomById(Long id) {
        Classroom room = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));
        return toDto(room);
    }

    public ClassroomDto createClassroom(ClassroomDto dto) {
        if (classroomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new IllegalArgumentException("Classroom number " + dto.getRoomNumber() + " already exists");
        }
        Classroom room = new Classroom(
                dto.getRoomNumber(),
                dto.getBuilding(),
                dto.getCapacity(),
                dto.getRoomType(),
                dto.isAvailable()
        );
        return toDto(classroomRepository.save(room));
    }

    public ClassroomDto updateClassroom(Long id, ClassroomDto dto) {
        Classroom room = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

        if (!room.getRoomNumber().equals(dto.getRoomNumber()) && classroomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new IllegalArgumentException("Classroom number " + dto.getRoomNumber() + " already in use");
        }

        room.setRoomNumber(dto.getRoomNumber());
        room.setBuilding(dto.getBuilding());
        room.setCapacity(dto.getCapacity());
        room.setRoomType(dto.getRoomType());
        room.setAvailable(dto.isAvailable());

        return toDto(classroomRepository.save(room));
    }

    public void deleteClassroom(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classroom not found with id: " + id);
        }
        classroomRepository.deleteById(id);
    }

    public List<ClassroomDto> getAvailableRoomsByType(RoomType roomType) {
        List<Classroom> list = (roomType != null)
                ? classroomRepository.findByRoomTypeAndAvailableTrue(roomType)
                : classroomRepository.findByAvailableTrue();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ClassroomDto toDto(Classroom entity) {
        if (entity == null) return null;
        ClassroomDto dto = new ClassroomDto();
        dto.setId(entity.getId());
        dto.setRoomNumber(entity.getRoomNumber());
        dto.setBuilding(entity.getBuilding());
        dto.setCapacity(entity.getCapacity());
        dto.setRoomType(entity.getRoomType());
        dto.setAvailable(entity.isAvailable());
        return dto;
    }
}
