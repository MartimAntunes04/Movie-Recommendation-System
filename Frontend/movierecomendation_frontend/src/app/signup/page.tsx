import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { TbMovie } from 'react-icons/tb';

export default function Page() {
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo/Brand */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-2">
            <TbMovie className="w-10 h-10 text-yellow-400" />
            <h1 className="text-4xl font-bold text-yellow-400">MRDB</h1>
          </div>
          <p className="text-gray-400 text-sm">Movie Recommendation Database</p>
        </div>

        <Card className="w-full backdrop-blur-sm bg-black/40 border-yellow-400/20 hover:shadow-2xl hover:shadow-yellow-400/20 transition-all duration-300 hover:border-yellow-400/40">
          <CardHeader className="text-center">
            <CardTitle className="text-2xl text-yellow-400">Join MRDB</CardTitle>
            <CardDescription className="text-gray-300">
              Create your account to discover amazing movies
            </CardDescription>
          </CardHeader>
          
          <CardContent>
            <form className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName" className="text-yellow-400 font-medium">
                    First Name
                  </Label>
                  <Input
                    id="firstName"
                    type="text"
                    placeholder="John"
                    className="bg-black/50 border-yellow-400/30 text-white placeholder:text-gray-500 focus:border-yellow-400"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="lastName" className="text-yellow-400 font-medium">
                    Last Name
                  </Label>
                  <Input
                    id="lastName"
                    type="text"
                    placeholder="Doe"
                    className="bg-black/50 border-yellow-400/30 text-white placeholder:text-gray-500 focus:border-yellow-400"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="email" className="text-yellow-400 font-medium">
                  Email Address
                </Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="john@example.com"
                  className="bg-black/50 border-yellow-400/30 text-white placeholder:text-gray-500 focus:border-yellow-400"
                  required
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password" className="text-yellow-400 font-medium">
                  Password
                </Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="Create a strong password"
                  className="bg-black/50 border-yellow-400/30 text-white placeholder:text-gray-500 focus:border-yellow-400"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-yellow-400 font-medium">
                  Confirm Password
                </Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="Confirm your password"
                  className="bg-black/50 border-yellow-400/30 text-white placeholder:text-gray-500 focus:border-yellow-400"
                  required
                />
              </div>

              <div className="flex items-center space-x-2">
                <input
                  id="terms"
                  type="checkbox"
                  className="rounded border-yellow-400/30 bg-black/50 text-yellow-400 focus:ring-yellow-400"
                  required
                />
                <Label htmlFor="terms" className="text-sm text-gray-300">
                  I agree to the{" "}
                  <a href="#" className="text-yellow-400 hover:text-yellow-300 underline">
                    Terms of Service
                  </a>{" "}
                  and{" "}
                  <a href="#" className="text-yellow-400 hover:text-yellow-300 underline">
                    Privacy Policy
                  </a>
                </Label>
              </div>
            </form>
          </CardContent>
          
          <CardFooter className="flex-col space-y-3">
            <Button 
              type="submit" 
              className="w-full bg-yellow-400 hover:bg-yellow-500 text-black font-semibold py-2.5 transition-colors"
            >
              Create Account
            </Button>
            
            <div className="relative w-full">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t border-gray-600" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-black px-2 text-gray-400">Or continue with</span>
              </div>
            </div>
            
            <Button 
              variant="outline" 
              className="w-full border-yellow-400/30 text-yellow-400 hover:bg-yellow-400/10 hover:border-yellow-400 hover:text-yellow-300"
            >
              <svg className="w-4 h-4 mr-2" viewBox="0 0 24 24">
                <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Continue with Google
            </Button>
          </CardFooter>
        </Card>

        {/* Sign In Link */}
        <div className="text-center mt-6">
          <p className="text-gray-400 text-sm">
            Already have an account?{" "}
            <a href="/login" className="text-yellow-400 hover:text-yellow-300 underline-offset-4 hover:underline font-medium">
              Sign in to MRDB
            </a>
          </p>
        </div>
      </div>
    </div>
  )
}
