dotnet clean
dotnet build --configuration Release
dotnet pack --configuration Release
cd .\bin\Release\
dotnet nuget push .\Robocode.TankRoyale.BotApi.0.9.5.nupkg -s D:\LocalNugetPackages