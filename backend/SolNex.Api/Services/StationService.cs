using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

public class StationService : IStationService
{
    private readonly IStationRepository _stationRepository;

    public StationService(IStationRepository stationRepository)
    {
        _stationRepository = stationRepository;
    }

    public async Task<IEnumerable<StationDto>> GetAllStationsAsync()
    {
        var stations = await _stationRepository.GetAllStationsAsync();
        return stations.Select(MapToDto);
    }

    public async Task<StationDto?> GetStationByIdAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id) 
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        return station != null ? MapToDto(station) : null;
    }

    public async Task<StationDto> CreateStationAsync(CreateStationDto createDto)
    {
        var station = new SolarStationInfo
        {
            StationId = createDto.StationId,
            StationName = createDto.StationName,
            Latitude = createDto.Latitude,
            Longitude = createDto.Longitude,
            CapacityKw = createDto.CapacityKw,
            TotalBatterySlots = createDto.TotalBatterySlots,
            AvailableBatterySlots = createDto.TotalBatterySlots, // Initially all slots available
            Status = StationStatus.Active,
            Schedule = createDto.Schedule ?? new Dictionary<string, string>(),
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        await _stationRepository.CreateStationAsync(station);
        return MapToDto(station);
    }

    public async Task<StationDto?> UpdateStationAsync(string id, UpdateStationDto updateDto)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        if (station == null) return null;

        bool updated = false;

        if (updateDto.StationName != null)
        {
            station.StationName = updateDto.StationName;
            updated = true;
        }
        if (updateDto.Latitude.HasValue)
        {
            station.Latitude = updateDto.Latitude.Value;
            updated = true;
        }
        if (updateDto.Longitude.HasValue)
        {
            station.Longitude = updateDto.Longitude.Value;
            updated = true;
        }
        if (updateDto.CapacityKw.HasValue)
        {
            station.CapacityKw = updateDto.CapacityKw.Value;
            updated = true;
        }
        if (updateDto.TotalBatterySlots.HasValue)
        {
            station.TotalBatterySlots = updateDto.TotalBatterySlots.Value;
            updated = true;
        }
        if (updateDto.AvailableBatterySlots.HasValue)
        {
            station.AvailableBatterySlots = updateDto.AvailableBatterySlots.Value;
            updated = true;
        }

        if (updated)
        {
            station.UpdatedAt = DateTime.UtcNow;
            await _stationRepository.UpdateStationAsync(station.Id!, station);
        }

        return MapToDto(station);
    }

    public async Task<StationDto?> DeactivateStationAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        if (station == null) return null;

        station.Status = StationStatus.Inactive;
        station.UpdatedAt = DateTime.UtcNow;

        await _stationRepository.UpdateStationAsync(station.Id!, station);
        return MapToDto(station);
    }

    public async Task<StationDto?> ActivateStationAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        if (station == null) return null;

        station.Status = StationStatus.Active;
        station.UpdatedAt = DateTime.UtcNow;

        await _stationRepository.UpdateStationAsync(station.Id!, station);
        return MapToDto(station);
    }

    public async Task<IEnumerable<StationDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusInKm)
    {
        var stations = await _stationRepository.GetNearbyStationsAsync(latitude, longitude, radiusInKm);
        return stations.Select(MapToDto);
    }

    public async Task<int?> GetStationAvailabilityAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        return station?.AvailableBatterySlots;
    }

    public async Task<Dictionary<string, string>?> GetStationScheduleAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        return station?.Schedule;
    }

    public async Task<StationDto?> UpdateStationScheduleAsync(string id, UpdateScheduleDto scheduleDto)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        if (station == null) return null;

        station.Schedule ??= new Dictionary<string, string>();
        
        // Merge the new schedule with the existing one
        foreach (var kvp in scheduleDto.Schedule)
        {
            station.Schedule[kvp.Key] = kvp.Value;
        }

        station.UpdatedAt = DateTime.UtcNow;

        await _stationRepository.UpdateStationAsync(station.Id!, station);
        return MapToDto(station);
    }

    private StationDto MapToDto(SolarStationInfo station)
    {
        return new StationDto
        {
            Id = station.Id,
            StationId = station.StationId,
            StationName = station.StationName,
            Latitude = station.Latitude,
            Longitude = station.Longitude,
            CapacityKw = station.CapacityKw,
            TotalBatterySlots = station.TotalBatterySlots,
            AvailableBatterySlots = station.AvailableBatterySlots,
            Status = station.Status.ToString(),
            Schedule = station.Schedule,
            CreatedAt = station.CreatedAt,
            UpdatedAt = station.UpdatedAt
        };
    }
}
