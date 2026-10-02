/*
 * ------------------------------------------------------------------
 * File Name: StationService.cs
 * Author: Rajapaksha T.M
 * Student ID: IT23235892
 * Date: 2026-09-24
 * Description: Service implementation for station operations.
 * ------------------------------------------------------------------
 */

using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Stations;
using SolNex.Api.DTOs.Dashboard;
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

    // Retrieves all solar stations and maps them to DTOs.
    public async Task<IEnumerable<StationDto>> GetAllStationsAsync()
    {
        var stations = await _stationRepository.GetAllStationsAsync();
        return stations.Select(MapToDto);
    }

    public async Task<IEnumerable<StationDto>> SearchStationsAsync(string? search, string? status)
    {
        var stations = await _stationRepository.SearchStationsAsync(search, status);
        return stations.Select(MapToDto);
    }

    // Retrieves a single solar station by ID and maps it to a DTO.
    public async Task<StationDto?> GetStationByIdAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id) 
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        return station != null ? MapToDto(station) : null;
    }

    // Computes the next available custom station ID by parsing existing IDs.
    public async Task<string> GetNextStationIdAsync()
    {
        var allStations = await _stationRepository.GetAllStationsAsync();
        int maxId = 0;
        
        // Iterate through existing stations to find the highest ID number
        foreach (var s in allStations)
        {
            // Only consider IDs that start with 'ST' and have valid numbers appended
            if (s.StationId != null && s.StationId.StartsWith("ST") && int.TryParse(s.StationId.Substring(2), out int num))
            {
                if (num > maxId)
                {
                    maxId = num;
                }
            }
        }
        
        // Increment the maximum ID and format it to 3 digits
        return $"ST{(maxId + 1):D3}";
    }

    // Creates a new solar station with an auto-generated ID.
    public async Task<StationDto> CreateStationAsync(CreateStationDto createDto)
    {
        // Obtain a unique generated station ID
        string generatedStationId = await GetNextStationIdAsync();

        // Construct the station entity mapped from the DTO
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

        // Persist the newly constructed entity to the database
        await _stationRepository.CreateStationAsync(station);
        
        // Convert to DTO before returning
        return MapToDto(station);
    }

    // Updates properties of an existing solar station.
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

    // Deactivates a station after ensuring it has no approved reservations.
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

    // Activates a deactivated solar station.
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

    // Permanently deletes a solar station, enforcing that it must be inactive first.
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

    // Finds the closest stations and maps to StationWithDistanceDto.
    public async Task<IEnumerable<StationWithDistanceDto>> GetClosestStationsAsync(double latitude, double longitude, int limit)
    {
        var closest = await _stationRepository.GetClosestStationsAsync(latitude, longitude, limit);
        return closest.Select(c => new StationWithDistanceDto
        {
            Station = MapToDto(c.Station),
            DistanceKm = c.Distance
        });
    }

    // Retrieves the current available battery slots for a given station.
    public async Task<int?> GetStationAvailabilityAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        return station?.AvailableBatterySlots;
    }

    // Retrieves the operating schedule map for a given station.
    public async Task<Dictionary<string, string>?> GetStationScheduleAsync(string id)
    {
        var station = await _stationRepository.GetStationByIdAsync(id)
                   ?? await _stationRepository.GetStationByStationIdAsync(id);

        return station?.Schedule;
    }

    // Merges new schedule entries into the existing station schedule.
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

    // Updates the available battery slots, verifying they don't exceed capacity.
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

    // Helper method to map a SolarStationInfo database entity to a StationDto.
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

    // Retrieves dashboard metrics and recent stations.
    public async Task<DashboardDto> GetDashboardDataAsync()
    {
        var stations = await _stationRepository.GetAllStationsAsync();
        var stationList = stations.ToList();
        var activeStationList = stationList.Where(s => s.Status == StationStatus.Active).ToList();
        
        var metrics = new DashboardMetricsDto
        {
            TotalStations = stationList.Count,
            ActiveStations = activeStationList.Count,
            InactiveStations = stationList.Count - activeStationList.Count,
            TotalBatterySlots = activeStationList.Sum(s => s.TotalBatterySlots),
            AvailableBatterySlots = activeStationList.Sum(s => s.AvailableBatterySlots)
        };

        var recentStations = stationList
            .OrderByDescending(s => s.CreatedAt)
            .Take(5)
            .Select(MapToDto)
            .ToList();

        return new DashboardDto
        {
            Metrics = metrics,
            RecentStations = recentStations
        };
    }
}
