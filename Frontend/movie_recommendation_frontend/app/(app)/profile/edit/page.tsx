"use client";
import { useAuth } from "@/contexts/AuthContext";
import { useUserProfile, clearUserProfileCache } from "@/hooks/useUserProfile";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { TbEye, TbEyeOff, TbArrowLeft, TbCheck, TbX } from 'react-icons/tb';
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState, useEffect } from "react";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export default function EditProfilePage() {
  const { token, isAuthenticated, loading: authLoading } = useAuth();
  const { profile, loading: profileLoading, refetch } = useUserProfile(token);
  const router = useRouter();


  const [username, setUsername] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [passwordError, setPasswordError] = useState("");

  // Pre-fill form when profile loads
  useEffect(() => {
    if (profile) {
      setUsername(profile.username || "");
      setFirstName(profile.firstName || "");
      setLastName(profile.lastName || "");
    }
  }, [profile]);

  const validatePasswords = (pass: string, confirmPass: string) => {
    if (confirmPass && pass && pass !== confirmPass) {
      setPasswordError("As senhas não coincidem");
      return false;
    } else {
      setPasswordError("");
      return true;
    }
  };

  const handlePasswordChange = (value: string) => {
    setPassword(value);
    validatePasswords(value, confirmPassword);
  };

  const handleConfirmPasswordChange = (value: string) => {
    setConfirmPassword(value);
    validatePasswords(password, value);
  };

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSuccessMessage("");

    // Validate passwords if provided
    if (password || confirmPassword) {
      if (!validatePasswords(password, confirmPassword)) {
        return;
      }
      if (password.length < 3) {
        setError("A senha deve ter pelo menos 3 caracteres");
        return;
      }
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/profile/update`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({
          username,
          firstName,
          lastName,
          password: password || undefined, // Only send if provided
        }),
      });

      const data = await response.json();

      if (data.success) {
        setSuccessMessage("Perfil atualizado com sucesso!");
        clearUserProfileCache(); // Limpa o cache
        
        // Clear password fields
        setPassword("");
        setConfirmPassword("");
        
        // Refetch profile to update cache com os novos dados
        await refetch();
        
          router.push("/profile");
      } else {
        setError(data.message || "Erro ao atualizar perfil");
      }
    } catch (err) {
      setError("Ocorreu um erro ao tentar atualizar o perfil.");
      console.error("Error updating profile:", err);
    } finally {
      setLoading(false);
    }
  }

  if (authLoading || profileLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-slate-300 border-t-yellow-500 mb-4"></div>
          <p className="text-slate-400">Carregando perfil...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null; // O AuthContext vai redirecionar
  }

  return (
    <div className="min-h-screen relative">
      <div className="relative z-10">
        <Card className="relative overflow-hidden bg-white/95 dark:bg-slate-900 backdrop-blur-md shadow-xl rounded-none">
          {/* Imagem de fundo do Card com opacidade de 10% */}
          <div 
            className="absolute inset-0 bg-cover bg-center bg-no-repeat opacity-10"
            style={{
              backgroundImage: 'url(https://www.ucicinemas.pt/media/1xolwnf3/banner-filmes-2025.jpg?width=1200&height=630&v=1db6bdbe09a9ac0)',
            }}
          ></div>
          
          {/* Conteúdo do Card por cima da imagem */}
          <div className="relative z-10">
            <CardContent>
              <div className="max-w-2xl mx-auto py-8">
                {/* Header */}
                <div className="mb-6">
                  <Link href="/profile">
                    <Button 
                      variant="ghost" 
                      className="mb-4 text-slate-600 dark:text-slate-400 hover:text-yellow-500"
                    >
                      <TbArrowLeft className="h-4 w-4 mr-2" />
                      Voltar ao Perfil
                    </Button>
                  </Link>
                  <CardHeader className="p-0">
                    <CardTitle className="text-2xl sm:text-3xl font-bold text-slate-900 dark:text-white">
                      Editar Perfil
                    </CardTitle>
                  </CardHeader>
                </div>

                {/* Form */}
                <form className="space-y-4" onSubmit={handleSubmit}>
                  {/* First Name and Last Name */}
                  <div className="flex gap-4">
                    <div className="flex-1 space-y-2">
                      <Label htmlFor="firstName">Primeiro Nome</Label>
                      <Input 
                        id="firstName" 
                        type="text"
                        autoComplete="given-name"
                        value={firstName}
                        onChange={e => setFirstName(e.target.value)} 
                        placeholder="João" 
                        required 
                        className="bg-white dark:bg-slate-800"
                      />
                    </div>

                    <div className="flex-1 space-y-2">
                      <Label htmlFor="lastName">Último Nome</Label>
                      <Input 
                        id="lastName" 
                        type="text" 
                        autoComplete="family-name"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)} 
                        placeholder="Silva" 
                        required 
                        className="bg-white dark:bg-slate-800"
                      />
                    </div>
                  </div>

                  {/* Username */}
                  <div className="space-y-2">
                    <Label htmlFor="username">Username</Label>
                    <Input 
                      id="username" 
                      type="text"
                      value={username}
                      onChange={e => setUsername(e.target.value)}
                      placeholder="joao123" 
                      required 
                      className="bg-white dark:bg-slate-800"
                    />
                  </div>
                  
                  
                  {/* Password */}
                  <div className="space-y-2">
                    <Label htmlFor="password">Nova Senha (opcional)</Label>
                    <div className="relative group">
                      <Input 
                        id="password" 
                        type={showPassword ? "text" : "password"} 
                        placeholder="Deixe em branco para manter a senha atual" 
                        className="pr-10 bg-white dark:bg-slate-800" 
                        value={password}
                        onChange={(e) => handlePasswordChange(e.target.value)}
                      />
                      <Button 
                        type="button" 
                        variant="ghost" 
                        className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-500 hover:text-yellow-400 group-hover:text-yellow-500 transition-all duration-500 hover:cursor-pointer" 
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? <TbEyeOff className="size-4" /> : <TbEye className="size-4" />}
                      </Button>
                    </div>
                  </div>
                  
                  {/* Confirm Password */}
                  {(password || confirmPassword) && (
                    <div className="space-y-2">
                      <Label htmlFor="confpassword">Confirmar Nova Senha</Label>
                      <div className="relative group">
                        <Input 
                          id="confpassword" 
                          type={showConfirmPassword ? "text" : "password"} 
                          placeholder="********" 
                          className={`pr-10 bg-white dark:bg-slate-800 ${passwordError ? 'border-red-500 focus:border-red-500' : confirmPassword && password === confirmPassword ? 'border-green-500 focus:border-green-500' : ''}`}
                          value={confirmPassword}
                          onChange={(e) => handleConfirmPasswordChange(e.target.value)}
                        />
                        <Button 
                          type="button" 
                          variant="ghost" 
                          className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-500 hover:text-yellow-400 group-hover:text-yellow-500 transition-all duration-500 hover:cursor-pointer" 
                          onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        >
                          {showConfirmPassword ? <TbEyeOff className="size-4" /> : <TbEye className="size-4" />}
                        </Button>
                        
                        {/* Ícone de validação */}
                        {confirmPassword && (
                          <div className="absolute right-10 top-1/2 -translate-y-1/2">
                            {password === confirmPassword ? (
                              <TbCheck className="size-4 text-green-500" />
                            ) : (
                              <TbX className="size-4 text-red-500" />
                            )}
                          </div>
                        )}
                      </div>
                      
                      {/* Mensagem de erro */}
                      {passwordError && (
                        <p className="text-sm text-red-500 flex items-center gap-1">
                          <TbX className="size-3" />
                          {passwordError}
                        </p>
                      )}
                      
                      {/* Mensagem de sucesso */}
                      {confirmPassword && password === confirmPassword && !passwordError && (
                        <p className="text-sm text-green-500 flex items-center gap-1">
                          <TbCheck className="size-3" />
                          As senhas coincidem
                        </p>
                      )}
                    </div>
                  )}

                  {/* Error Message */}
                  {error && (
                    <div className="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
                      <p className="text-sm text-red-600 dark:text-red-400 flex items-center gap-1">
                        <TbX className="size-4" />
                        {error}
                      </p>
                    </div>
                  )}

                  {/* Success Message */}
                  {successMessage && (
                    <div className="p-3 rounded-lg bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800">
                      <p className="text-sm text-green-600 dark:text-green-400 flex items-center gap-1">
                        <TbCheck className="size-4" />
                        {successMessage}
                      </p>
                    </div>
                  )}
                  
                  {/* Submit Button */}
                  <div className="flex gap-4 pt-4">
                    <Button
                      type="submit"
                      className={`flex-1 bg-linear-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-white transition-all duration-500 ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
                      disabled={loading || !!passwordError}
                    >
                      {loading ? "A guardar..." : "Guardar Alterações"}
                    </Button>
                    <Link href="/profile" className="flex-1">
                      <Button
                        type="button"
                        variant="outline"
                        className="w-full"
                      >
                        Cancelar
                      </Button>
                    </Link>
                  </div>
                </form>
              </div>
            </CardContent>
          </div>
        </Card>
      </div>
    </div>
  );
}
