using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Stations;
using SolNex.Api.Models;
using SolNex.Api.Repositories;
using SolNex.Api.Repositories.Stations;
using SolNex.Api.Repositories.Reservations;

namespace SolNex.Api.Services.Stations;

public class StationService : IStationService
{
    private readonly IStationRepository _stationRepository;
    private readonly IEnergyReservationRepository _reservationRepository;

    public StationService(IStationRepository stationRepository, IEnergyReservationRepository reservationRepository)
    {
        _stationRepository = stationRepository;
        _reservationRepository = reservationRepository;
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
        var allStations = await _stationRepository.GetAllStationsAsync();
        int maxId = 0;
        foreach (var s in allStations)
        {
            if (s.StationId != null && s.StationId.StartsWith("ST") && int.TryParse(s.StationId.Substring(2), out int num))
            {
                if (num > maxId)
                {
                    maxId = num;
                }
            }
        }
        string generatedStationId = $"ST{(maxId + 1):D3}";

        var station = new SolarStationInfo
        {
            StationId = generatedStationId,
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

        var approvedReservations = await _reservationRepository.SearchReservationsAsync(station.StationId, ReservationStatus.Approved.ToString());
        if (approvedReservations.Any())
        {
            throw new InvalidOperationException("Cannot deactivate a station that has approved reservations.");
        }

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

    public async Task<bool> DeleteStationAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        if (station == null) return false;

        if (station.Status != StationStatus.Inactive)
        {
            throw new InvalidOperationException("Only deactivated stations can be deleted.");
        }

        await _stationRepository.DeleteStationAsync(station.Id!);
        return true;
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

    public async Task<StationDto?> UpdateBatterySlotsAsync(string id, UpdateBatterySlotsDto updateDto)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        if (station == null) return null;

        if (updateDto.AvailableBatterySlots > station.TotalBatterySlots)
        {
            throw new InvalidOperationException("Available battery slots cannot exceed total battery slots.");
        }

        station.AvailableBatterySlots = updateDto.AvailableBatterySlots;
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
